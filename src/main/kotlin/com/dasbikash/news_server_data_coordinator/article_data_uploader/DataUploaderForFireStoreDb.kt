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

import com.dasbikash.news_server_data_coordinator.firebase.FireStoreDataUtils
import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils

class DataUploaderForFireStoreDb : DataUploader() {

    override fun getUploadDestinationInfo(): UploadDestinationInfo {
        return UploadDestinationInfo.FIRE_STORE_DB
    }

    override fun getMaxArticleAgeInDays(): Int {
        return MAX_ARTICLE_AGE_DAYS
    }

    override fun uploadArticles(articlesForUpload: List<Article>): Boolean {
        FireStoreDataUtils.writeArticleData(articlesForUpload)
        LoggerUtils.logOnConsole("${articlesForUpload.size} articles uploaded to ${getUploadDestinationInfo().articleUploadTarget.name}")
        return true
    }

    override fun maxArticleCountForUpload(): Int {
        return MAX_ARTICLE_COUNT_FOR_UPLOAD
    }

    override fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                                   newspapers: Collection<Newspaper>, pages: Collection<Page>,
                                   pageGroups:Collection<PageGroup>) {
        FireStoreDataUtils.uploadNewSettings(languages, countries, newspapers, pages,pageGroups)
    }

    override fun addToServerUploadTimeLog() {
        FireStoreDataUtils.addToServerUploadTimeLog()
    }

    override fun nukeOldSettings() {
        FireStoreDataUtils.nukeAppSettings()
    }

    override fun deleteArticleFromServer(article: Article): Boolean {
        return FireStoreDataUtils.deleteArticleFromServer(article)
    }

    override fun getMaxArticleCountForPage() = MAX_ARTICLE_COUNT_FOR_PAGE

    override fun getDailyArticleDeletionLimit() = DAILY_ARTICLE_DELETION_LIMIT

    override fun getMaxArticleDeletionChunkSize() = MAX_ARTICLE_DELETION_CHUNK_SIZE

    override fun getArticleDeletionRoutineRunningHour() = ARTICLE_DELETION_ROUTINE_RUNNING_HOUR

    companion object {
        private const val MAX_ARTICLE_AGE_DAYS = 30
        private const val MAX_ARTICLE_COUNT_FOR_UPLOAD = 400
        private const val MAX_ARTICLE_COUNT_FOR_PAGE = 150
        private const val DAILY_ARTICLE_DELETION_LIMIT = 3000//8000
        private const val MAX_ARTICLE_DELETION_CHUNK_SIZE = 400
        private const val ARTICLE_DELETION_ROUTINE_RUNNING_HOUR = 9
    }
}