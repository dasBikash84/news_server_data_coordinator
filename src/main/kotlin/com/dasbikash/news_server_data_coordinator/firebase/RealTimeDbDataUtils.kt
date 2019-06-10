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


object RealTimeDbDataUtils {

    private val mArticleDataRootReference:DatabaseReference = RealTimeDbRefUtils.getArticleDataRootReference()

    fun writeArticleData(articleList:List<Article>){
        val futureList = mutableListOf<ApiFuture<Void>>()

        articleList.asSequence().forEach {
            futureList.add(mArticleDataRootReference.child(it.page!!.id).child(it.id).setValueAsync(ArticleForFB.fromArticle(it)))
        }
        futureList.asSequence().forEach {
            while (!it.isDone){}
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


}
