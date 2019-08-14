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

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.google.api.core.ApiFuture
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ServerValue
import org.hibernate.Session


object RealTimeDbDataUtils {

    private val mArticleDataRootReference: DatabaseReference = RealTimeDbRefUtils.getArticleDataRootReference()
    private val mNewsCategoriesArticleInfoReference: DatabaseReference = RealTimeDbRefUtils.getNewsCategoriesArticleInfoRef()

    private val newsCategoryMap = mutableMapOf<String,NewsCategory>()
    private val newsCategoryEntryMap = mutableMapOf<Int,NewsCategoryEntry>()

    private var lastNewsCategoryMapUpdateTime = 0L
    private var lastNewsCategoryEntryMapUpdateTime = 0L
    private const val MAP_UPDATE_INTERVAL = 10*60*1000L // 10 mins

    private fun getNewsCategoryMap(session: Session):Map<String,NewsCategory>{
        if (newsCategoryMap.isEmpty() ||
                (System.currentTimeMillis() - lastNewsCategoryMapUpdateTime)> MAP_UPDATE_INTERVAL){
            newsCategoryMap.clear()
            val newMap = DatabaseUtils.getNewsCategoryMap(session)
            newMap.keys.asSequence().forEach { newsCategoryMap.put(it,newMap.get(it)!!) }
            lastNewsCategoryMapUpdateTime = System.currentTimeMillis()
        }
        return newsCategoryMap.toMap()
    }

    private fun getNewsCategoryEntryMap(session: Session):Map<Int,NewsCategoryEntry>{
        if (newsCategoryEntryMap.isEmpty() ||
                (System.currentTimeMillis() - lastNewsCategoryEntryMapUpdateTime)> MAP_UPDATE_INTERVAL){
            newsCategoryEntryMap.clear()
            val newMap = DatabaseUtils.getNewsCategoryEntryMap(session)
            newMap.keys.asSequence().forEach { newsCategoryEntryMap.put(it,newMap.get(it)!!) }
            lastNewsCategoryEntryMapUpdateTime = System.currentTimeMillis()
        }
        return newsCategoryEntryMap.toMap()
    }

    fun writeArticleData(articleList: List<Article>, session: Session) {
        val futureList = mutableListOf<ApiFuture<Void>>()

        articleList.asSequence().forEach {
            val article = it
            val node: DatabaseReference
            if (it.page!!.topLevelPage!!) {
                node = mArticleDataRootReference.child(it.page!!.id).child(it.id)
            } else {
                node = mArticleDataRootReference.child(it.page!!.parentPageId!!).child(it.id)
            }
            futureList.add(node.setValueAsync(ArticleForRTDB.fromArticle(it)))
            getNewsCategoriesForPage(it.page!!, session).asSequence().forEach {
                futureList.add(mNewsCategoriesArticleInfoReference.child(it.id).child(article.id).setValueAsync(
                        NewsCategoriesArticleInfoEntry.getInstance(article)
                ))
            }
        }
        futureList.asSequence().forEach {
            while (!it.isDone) {
                Thread.sleep(10)
            }
        }
    }

    private fun getNewsCategoriesForPage(page: Page, session: Session): List<NewsCategory> {
        val parentPageId = when (page.topLevelPage ?: false) {
            true -> page.id
            false -> page.parentPageId!!
        }
        return getNewsCategoryEntryMap(session).values
                .filter { (it.getPage()!!.id == parentPageId) ||(it.getPage()!!.id == page.id) }
                .map { getNewsCategoryMap(session).get(it.getNewsCategory()!!.id)!! }
                .toList()
    }

    fun clearArticleDataForPage(page: Page) {
        println(mArticleDataRootReference.child(page.id).path.toString())
        val task = mArticleDataRootReference.child(page.id).setValueAsync(null)
        while (!task.isDone) {
            Thread.sleep(10)
        }
    }

    private val MAX_WAITING_TIME_FOR_DATA_DELETION = 2*60*1000L //2 mins

    fun clearData(databaseReference: DatabaseReference):Boolean {
        val task = databaseReference.setValueAsync(null)
        val startTime = System.currentTimeMillis()
        while (!task.isDone || (System.currentTimeMillis()-startTime < MAX_WAITING_TIME_FOR_DATA_DELETION)){
            Thread.sleep(10L)
        }
        return (System.currentTimeMillis()-startTime) < MAX_WAITING_TIME_FOR_DATA_DELETION
    }

    fun nukeAppSettings() {
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
                          newspapers: Collection<Newspaper>, pages: Collection<Page>,
                          newsCategories: Collection<NewsCategory>) {
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
        newsCategories.asSequence().forEach {
            listOfFuture.add(RealTimeDbRefUtils.getNewsCategoriesRef().child(it.id).setValueAsync(it))
        }

        listOfFuture.asSequence().forEach {
            while (!it.isDone) {
            }
        }
    }

    fun addToServerUploadTimeLog() {
        val task = RealTimeDbRefUtils.getSettingsUpdateTimeRef().push().setValueAsync(ServerValue.TIMESTAMP)
        while (!task.isDone) {
        }
    }

    fun deleteArticleFromServer(article: Article,session: Session): Boolean {

        val futureList = mutableListOf<ApiFuture<Void>>()

        futureList.add(mArticleDataRootReference.child(article.page!!.id).child(article.id).setValueAsync(null))

        getNewsCategoriesForPage(article.page!!, session).asSequence().forEach {
            futureList.add(mNewsCategoriesArticleInfoReference.child(it.id).child(article.id).setValueAsync(null))
        }
        futureList.asSequence().forEach {while (!it.isDone) {Thread.sleep(10)}}
        return true
    }

    fun uploadKeyWordSearchResultData(keyWordSearchResult: KeyWordSearchResult, session: Session) {
        val searchResultMap = keyWordSearchResult.getSearchResultMap(session)
        if (searchResultMap.isNotEmpty()) {
            val task = RealTimeDbRefUtils.getKeyWordSearchResultNode()
                    .child(keyWordSearchResult.keyWord!!).setValueAsync(searchResultMap)
            while (!task.isDone) {
            }
        }
    }

    fun uploadKeyWordSearchResultData(keyWordSearchResults: List<KeyWordSearchResult>, session: Session) {
        val futureList = mutableListOf<ApiFuture<Void>>()
        keyWordSearchResults.asSequence().forEach {
            val searchResultMap = it.getSearchResultMap(session)
            if (searchResultMap.isNotEmpty()) {
                futureList.add(RealTimeDbRefUtils.getKeyWordSearchResultNode()
                        .child(it.keyWord!!).setValueAsync(searchResultMap))

                val valueForSearchKeyNode: Any?
                if (searchResultMap.values.filter { it != null }.count() > 0) {
                    valueForSearchKeyNode = true
                } else {
                    valueForSearchKeyNode = null
                }
                futureList.add(RealTimeDbRefUtils.getSearchKeyWordsNode()
                        .child(it.keyWord!!).setValueAsync(valueForSearchKeyNode))

            } else {
                futureList.add(RealTimeDbRefUtils.getKeyWordSearchResultNode()
                        .child(it.keyWord!!).setValueAsync(null))
                futureList.add(RealTimeDbRefUtils.getSearchKeyWordsNode()
                        .child(it.keyWord!!).setValueAsync(null))
            }
        }
        futureList.asSequence().forEach {
            while (!it.isDone) {
            }
        }
    }
}

data class NewsCategoriesArticleInfoEntry(
        val articleId: String,
        val pageId:String,
        val publicationTimeRTDB: Long
){
    companion object{
        fun getInstance(article:Article):NewsCategoriesArticleInfoEntry{
            val page = article.page!!
            val parentPageId = when (page.topLevelPage ?: false) {
                true -> page.id
                false -> page.parentPageId!!
            }
            return NewsCategoriesArticleInfoEntry(article.id,parentPageId,article.publicationTime!!.time)
        }
    }
}
