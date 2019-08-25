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
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import com.google.api.core.ApiFuture
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.WriteBatch
import com.google.cloud.firestore.WriteResult
import org.hibernate.Session


object FireStoreDataUtils {

    private const val MAX_BATCH_SIZE_FOR_WRITE = 400
    private const val MAX_BATCH_SIZE_FOR_DELETE = 100

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

    fun writeArticleData(articles: List<Article>,session: Session) {
        if (articles.size > MAX_BATCH_SIZE_FOR_WRITE) {
            throw IllegalArgumentException()
        }

        val batch = FireBaseConUtils.mFireStoreCon.batch()

        articles.asSequence().forEach {
            val newsCategories = getNewsCategoriesForPage(it.page!!, session)
            batch.set(FireStoreRefUtils.getArticleCollectionRef().document(it.id), ArticleForFB.fromArticle2(it,newsCategories))
        }
        val future = batch.commit()
        for (result in future.get()) {
        }
    }

    fun nukeAppSettings() {
        deleteCollectionInBatch(FireStoreRefUtils.getPageSettingsCollectionRef())
        deleteCollectionInBatch(FireStoreRefUtils.getNewspaperSettingsCollectionRef())
        deleteCollectionInBatch(FireStoreRefUtils.getLanguageSettingsCollectionRef())
        deleteCollectionInBatch(FireStoreRefUtils.getCountrySettingsCollectionRef())
    }

    fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                          newspapers: Collection<Newspaper>, pages: Collection<Page>,
                          newsCategories: Collection<NewsCategory>)
            : Boolean {
        if (languages.isNotEmpty()) {
            val languageMap = mutableMapOf<LanguageForFB, String>()
            languages.asSequence().forEach {
                languageMap.put(LanguageForFB.getFromLanguage(it), it.id)
            }
            if (!writeToCollection(languageMap, FireStoreRefUtils.getLanguageSettingsCollectionRef())) {
                return false
            }
        }

        if (countries.isNotEmpty()) {
            val countryMap = mutableMapOf<CountryForFB, String>()
            countries.asSequence().forEach {
                countryMap.put(CountryForFB.getFromCountry(it), it.name)
            }
            if (!writeToCollection(countryMap, FireStoreRefUtils.getCountrySettingsCollectionRef())) {
                return false
            }
        }

        if (newspapers.isNotEmpty()) {
            val newspaperMap = mutableMapOf<NewspaperForFB, String>()
            newspapers.asSequence().forEach {
                newspaperMap.put(NewspaperForFB.getFromNewspaper(it), it.id)
            }
            if (!writeToCollection(newspaperMap, FireStoreRefUtils.getNewspaperSettingsCollectionRef())) {
                return false
            }
        }

        if (pages.isNotEmpty()) {
            val pagesMap = mutableMapOf<PageForFB, String>()
            pages.asSequence().forEach {
                pagesMap.put(PageForFB.getFromPage(it), it.id)
            }

            if (!writeToCollection(pagesMap, FireStoreRefUtils.getPageSettingsCollectionRef())) {
                return false
            }
        }

        if (newsCategories.isNotEmpty()) {
            val newsCategoryMap = mutableMapOf<NewsCategory, String>()
            newsCategories.asSequence().forEach {
                newsCategoryMap.put(it, it.id)
            }

            if (!writeToCollection(newsCategoryMap, FireStoreRefUtils.getNewsCategorySettingsCollectionRef())) {
                return false
            }
        }

        return true
    }

    fun addToServerUploadTimeLog() {
        val task = FireStoreRefUtils.getSettingsUpdateTimeCollectionRef()
                .document().set(UpdateTimeEntry(FieldValue.serverTimestamp()))
        task.get()
    }

    private fun <T : Any> writeToCollection(dataMap: Map<T, String?>, collectionRef: CollectionReference): Boolean {
        val contents = dataMap.keys

        var contentCount = 0
        var batch: WriteBatch? = null
        contents.asSequence().forEach {
            if (contentCount == 0) {
                batch = FireBaseConUtils.mFireStoreCon.batch()
            }
            contentCount++
            if (dataMap.get(it) == null) {
                batch!!.set(collectionRef.document(), it)
            } else {
                batch!!.set(collectionRef.document(dataMap.get(it) as String), it)
            }
            if (contentCount == MAX_BATCH_SIZE_FOR_WRITE) {
                val future = batch!!.commit()
                for (result in future.get()) {
                }
                contentCount = 0
            }
        }
        if (contentCount > 0) {
            val future = batch!!.commit()
            for (result in future.get()) {
            }
        }
        return true

    }

    /** Delete a collection in batches to avoid out-of-memory errors.
     * Batch size may be tuned based on document size (atmost 1MB) and application requirements.
     */
    private fun deleteCollectionInBatch(collection: CollectionReference, batchSize: Int = MAX_BATCH_SIZE_FOR_DELETE) {
        var maxRetry = 1
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            val future = collection.limit(batchSize).get()
            var deleted = 0
            // future.get() blocks on document retrieval
            val documents = future.get().getDocuments()
            for (document in documents) {
                document.getReference().delete()
                ++deleted
            }
            if (deleted >= batchSize) {
                // retrieve and delete another batch
                deleteCollectionInBatch(collection, batchSize)
            }
        } catch (e: Exception) {
            maxRetry--
            LoggerUtils.logOnConsole("Error deleting collection : " + e.message)
            if (maxRetry >= 0) {
                deleteCollectionInBatch(collection, batchSize)
            } else {
                throw e
            }
        }

    }

    fun deleteArticleFromServer(article: Article): Boolean {
        val future = FireStoreRefUtils.getArticleCollectionRef().document(article.id).delete()
        while (future.isDone){}
        return true
    }

    fun deleteArticlesFromServer(articles: List<Article>) {
        val futureList = mutableListOf<ApiFuture<WriteResult>>()
        articles.asSequence().forEach {
            futureList.add(FireStoreRefUtils.getArticleCollectionRef().document(it.id).delete())
        }
        futureList.asSequence().forEach {while (!it.isDone) {Thread.sleep(10)}}
    }

}

class UpdateTimeEntry(val updateTime: FieldValue)
