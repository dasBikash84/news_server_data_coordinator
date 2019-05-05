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
import java.lang.IllegalArgumentException
import com.google.cloud.firestore.CollectionReference
import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.WriteBatch


object FireStoreDataUtils {

    private const val MAX_BATCH_SIZE_FOR_WRITE = 400
    private const val MAX_BATCH_SIZE_FOR_DELETE = 100

    fun writeArticleData(articles:List<Article>){
        if (articles.size> MAX_BATCH_SIZE_FOR_WRITE){
            throw IllegalArgumentException()
        }

        val batch = FireBaseConUtils.mFireStoreCon.batch()

        articles.asSequence().forEach {
            batch.set(FireStoreRefUtils.getArticleCollectionRef().document(it.id), ArticleForFB.fromArticle(it))
        }
        val future = batch.commit()
        while (!future.isDone){}
    }

    fun nukeAppSettings() {
        deleteCollectionInBatch(FireStoreRefUtils.getPageSettingsCollectionRef())
        deleteCollectionInBatch(FireStoreRefUtils.getNewspaperSettingsCollectionRef())
        deleteCollectionInBatch(FireStoreRefUtils.getLanguageSettingsCollectionRef())
        deleteCollectionInBatch(FireStoreRefUtils.getCountrySettingsCollectionRef())
    }

    fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                          newspapers: Collection<Newspaper>, pages: Collection<Page>):Boolean {
        val languageMap = mutableMapOf<LanguageForFB,String>()
        languages.asSequence().forEach {
            languageMap.put(LanguageForFB.getFromLanguage(it),it.id)
        }
        if (!writeToCollection(languageMap,FireStoreRefUtils.getLanguageSettingsCollectionRef())){
            return false
        }

        val countryMap = mutableMapOf<CountryForFB,String>()
        countries.asSequence().forEach {
            countryMap.put(CountryForFB.getFromCountry(it),it.name)
        }
        if (!writeToCollection(countryMap,FireStoreRefUtils.getCountrySettingsCollectionRef())){
            return false
        }

        val newspaperMap = mutableMapOf<NewspaperForFB,String>()
        newspapers.asSequence().forEach {
            newspaperMap.put(NewspaperForFB.getFromNewspaper(it),it.id)
        }
        if (!writeToCollection(newspaperMap,FireStoreRefUtils.getNewspaperSettingsCollectionRef())){
            return false
        }

        val pagesMap = mutableMapOf<PageForFB,String>()
        pages.asSequence().forEach {
            pagesMap.put(PageForFB.getFromPage(it),it.id)
        }

        if (!writeToCollection(pagesMap,FireStoreRefUtils.getPageSettingsCollectionRef())){
            return false
        }

        return true
    }

    fun addToServerUploadTimeLog() {
        val task = FireStoreRefUtils.getSettingsUpdateTimeCollectionRef()
                                            .document().set(UpdateTimeEntry(FieldValue.serverTimestamp()))

        while (!task.isDone){}
    }

    private fun <T:Any> writeToCollection(dataMap:Map<T,String?>, collectionRef: CollectionReference):Boolean{
        val contents = dataMap.keys

        var contentCount = 0
        var batch:WriteBatch? = null
        try {
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
                    while (!future.isDone) {
                    }
                    contentCount = 0
                }
            }
            if (contentCount>0){
                val future = batch!!.commit()
                while (!future.isDone) {
                }
            }
        }catch (ex:Exception){
            ex.printStackTrace()
            System.err.println("Error writing collection : " + ex.message)
            return false
        }
        return true

    }

    /** Delete a collection in batches to avoid out-of-memory errors.
     * Batch size may be tuned based on document size (atmost 1MB) and application requirements.
     */
    private fun deleteCollectionInBatch(collection: CollectionReference, batchSize: Int= MAX_BATCH_SIZE_FOR_DELETE) {
        var maxRetry = 3
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
            System.err.println("Error deleting collection : " + e.message)
            if (maxRetry >=0){
                deleteCollectionInBatch(collection, batchSize)
            }else{
                throw e
            }
        }

    }

}

class UpdateTimeEntry(val updateTime: FieldValue)
