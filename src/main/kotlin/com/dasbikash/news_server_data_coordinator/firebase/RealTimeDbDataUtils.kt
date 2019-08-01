/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import com.google.api.core.ApiFuture
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import org.hibernate.Session


object RealTimeDbDataUtils {

    private val mArticleDataRootReference:DatabaseReference = RealTimeDbRefUtils.getArticleDataRootReference()

    fun writeArticleData(articleList:List<Article>){
        val futureList = mutableListOf<ApiFuture<Void>>()

        articleList.asSequence().forEach {
            val node:DatabaseReference
            if (it.page!!.topLevelPage!!) {
                node=mArticleDataRootReference.child(it.page!!.id).child(it.id)
            }else{
                node=mArticleDataRootReference.child(it.page!!.parentPageId!!).child(it.id)
            }
            futureList.add(node.setValueAsync(ArticleForRTDB.fromArticle(it)))
        }
        futureList.asSequence().forEach {
            while (!it.isDone){}
        }
    }

    fun clearArticleDataForPage(page: Page){
        println(mArticleDataRootReference.child(page.id).path.toString())
        val task = mArticleDataRootReference.child(page.id).setValueAsync(null)
        while (!task.isDone){
            Thread.sleep(10)
        }
    }

    fun clearAllArticleData(){
//        val task = mArticleDataRootReference.setValueAsync(null)
//        while (!task.isDone){
//            println("Waiting for data deletion.")
//            Thread.sleep(1000L)
//        }
    }

    fun nukeAppSettings(){
        val listOfFuture = mutableListOf<ApiFuture<Void>>()
        listOfFuture.add(RealTimeDbRefUtils.getPagesRef().setValueAsync(null))
        listOfFuture.add(RealTimeDbRefUtils.getNewspapersRef().setValueAsync(null))
        listOfFuture.add(RealTimeDbRefUtils.getCountriesRef().setValueAsync(null))
        listOfFuture.add(RealTimeDbRefUtils.getLanguagesRef().setValueAsync(null))
        listOfFuture.asSequence().forEach {
            while (!it.isDone) {
            }
        }
    }

    fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                          newspapers: Collection<Newspaper>, pages: Collection<Page>,pageGroups:Collection<PageGroup>) {
        val listOfFuture = mutableListOf<ApiFuture<Void>>()
        languages.asSequence().forEach {
            listOfFuture.add(RealTimeDbRefUtils.getLanguagesRef().child(it.id).setValueAsync(LanguageForFB.getFromLanguage(it)))
        }
        countries.asSequence().forEach {
            listOfFuture.add(RealTimeDbRefUtils.getCountriesRef().child(it.name).setValueAsync(CountryForFB.getFromCountry(it)))
        }
        newspapers.asSequence().forEach {
            listOfFuture.add(RealTimeDbRefUtils.getNewspapersRef().child(it.id).setValueAsync(NewspaperForFB.getFromNewspaper(it)))
        }
        pages.asSequence().forEach {
            listOfFuture.add(RealTimeDbRefUtils.getPagesRef().child(it.id).setValueAsync(PageForFB.getFromPage(it)))
        }

        pageGroups.asSequence().forEach {
            LoggerUtils.logOnConsole(it.toString())
            listOfFuture.add(RealTimeDbRefUtils.getPageGroupsRef().child(it.name).setValueAsync(PageGroupForFB.getFromPageGroup(it)))
        }

        listOfFuture.asSequence().forEach {
            while (!it.isDone) {}
        }
    }

    fun addToServerUploadTimeLog() {
        val task = RealTimeDbRefUtils.getSettingsUpdateTimeRef().push().setValueAsync(ServerValue.TIMESTAMP)
        while (!task.isDone) {}
    }

    fun deleteArticleFromServer(article: Article): Boolean {
        val future = mArticleDataRootReference.child(article.page!!.id).child(article.id).setValueAsync(null)
        while (future.isDone){}
        return true
    }

    fun uploadKeyWordSearchResultData(keyWordSearchResult: KeyWordSearchResult,session: Session){
        val searchResultMap = keyWordSearchResult.getSearchResultMap(session)
        if (searchResultMap.isNotEmpty()){
            val task = RealTimeDbRefUtils.getKeyWordSearchResultNode()
                                            .child(keyWordSearchResult.keyWord!!).setValueAsync(searchResultMap)
            while (!task.isDone){}
        }
    }

    fun uploadKeyWordSearchResultData(keyWordSearchResults: List<KeyWordSearchResult>,session: Session){
        val futureList = mutableListOf<ApiFuture<Void>>()
        keyWordSearchResults.asSequence().forEach {
            val searchResultMap = it.getSearchResultMap(session)
            if (searchResultMap.isNotEmpty()){
                futureList.add(RealTimeDbRefUtils.getKeyWordSearchResultNode()
                                    .child(it.keyWord!!).setValueAsync(searchResultMap))

                val valueForSearchKeyNode:Any?
                if (searchResultMap.values.filter { it!=null }.count() > 0){
                    valueForSearchKeyNode = true
                }else{
                    valueForSearchKeyNode = null
                }
                futureList.add(RealTimeDbRefUtils.getSearchKeyWordsNode()
                                    .child(it.keyWord!!).setValueAsync(valueForSearchKeyNode))

            }else{
                futureList.add(RealTimeDbRefUtils.getKeyWordSearchResultNode()
                        .child(it.keyWord!!).setValueAsync(null))
                futureList.add(RealTimeDbRefUtils.getSearchKeyWordsNode()
                        .child(it.keyWord!!).setValueAsync(null))
            }
        }
        futureList.asSequence().forEach {
            while (!it.isDone){}
        }
    }

}
