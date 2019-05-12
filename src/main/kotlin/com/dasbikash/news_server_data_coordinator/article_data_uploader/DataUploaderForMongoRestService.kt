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

package com.dasbikash.news_server_data_coordinator.article_data_uploader

import com.dasbikash.news_server_data_coordinator.model.db_entity.*

class DataUploaderForMongoRestService:DataUploader() {

    override fun getUploadDestinationInfo(): UploadDestinationInfo {
        return UploadDestinationInfo.MONGO_REST_SERVICE
    }

    override fun getMaxArticleAgeInDays(): Int {
        return MAX_ARTICLE_AGE_DAYS
    }

    override fun uploadArticles(articlesForUpload: List<Article>): Boolean {
        try {
            println("Going to upload data")
            TODO("Data uploader not implemented")
            println("Upload successful")
            return true
        }catch (ex:Exception){
            println("Upload failure")
            ex.printStackTrace()
            return false
        }
    }

    override fun maxArticleCountForUpload(): Int {
        return MAX_ARTICLE_COUNT_FOR_UPLOAD
    }

    override fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                                   newspapers: Collection<Newspaper>, pages: Collection<Page>) {
        TODO()
    }

    override fun addToServerUploadTimeLog() {
        TODO()
    }

    override fun nukeOldSettings() {
        TODO()
    }

    companion object{
        private const val MAX_ARTICLE_AGE_DAYS = 90
        private const val MAX_ARTICLE_COUNT_FOR_UPLOAD = 100
    }
}