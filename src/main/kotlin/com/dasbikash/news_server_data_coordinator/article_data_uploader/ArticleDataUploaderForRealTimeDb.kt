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

import com.dasbikash.news_server_data_coordinator.firebase.*
import com.dasbikash.news_server_data_coordinator.model.db_entity.*

class ArticleDataUploaderForRealTimeDb : ArticleDataUploader() {

    override fun getUploadDestinationInfo(): UploadDestinationInfo {
        return UploadDestinationInfo.REAL_TIME_DB
    }

    override fun getMaxArticleAgeInDays(): Int {
        return MAX_ARTICLE_AGE_DAYS
    }

    override fun uploadArticles(articlesForUpload: List<Article>): Boolean {
        try {
            println("Going to upload data")
            RealTimeDbDataUtils.writeArticleData(articlesForUpload)
            println("Upload successful")
            return true
        } catch (ex: Exception) {
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
        RealTimeDbDataUtils.uploadNewSettings(languages, countries, newspapers, pages)
    }

    override fun addToServerUploadTimeLog() {
        RealTimeDbDataUtils.addToServerUploadTimeLog()
    }

    override fun nukeOldSettings() {
        RealTimeDbDataUtils.nukeAppSettings()
    }

    companion object {
        private const val MAX_ARTICLE_AGE_DAYS = 20
        private const val MAX_ARTICLE_COUNT_FOR_UPLOAD = 100
    }
}