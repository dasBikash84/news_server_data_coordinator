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

import com.dasbikash.news_server_data_coordinator.firebase.RealTimeDbDataUtils
import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.dasbikash.news_server_data_coordinator.utils.DateUtils
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import org.hibernate.Session

class DataUploaderForRealTimeDb : DataUploader() {

    override fun getUploadDestinationInfo(): UploadDestinationInfo {
        return UploadDestinationInfo.REAL_TIME_DB
    }

    override fun uploadArticles(articlesForUpload: List<Article>,session: Session): Boolean {
        try {
            RealTimeDbDataUtils.writeArticleData(articlesForUpload,session)
            LoggerUtils.logOnConsole("${articlesForUpload.size} articles uploaded to ${getUploadDestinationInfo().articleUploadTarget.name}")
            return true
        } catch (ex: Exception) {
            LoggerUtils.logOnConsole("Article upload failure to ${getUploadDestinationInfo().articleUploadTarget}")
            ex.printStackTrace()
            return false
        }
    }

    override fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                                   newspapers: Collection<Newspaper>, pages: Collection<Page>,
                                   newsCategories: Collection<NewsCategory>) {
        RealTimeDbDataUtils.uploadNewSettings(languages, countries, newspapers, pages,newsCategories)
    }

    override fun addToServerUploadTimeLog() {
        RealTimeDbDataUtils.addToServerUploadTimeLog()
    }

    override fun nukeOldSettings() {
        RealTimeDbDataUtils.nukeAppSettings()
    }

    override fun deleteArticleFromServer(article: Article,session: Session): Boolean {
        return RealTimeDbDataUtils.deleteArticleFromServer(article,session)
    }

    override fun deleteArticlesFromServer(articles: List<Article>, session: Session) {
        return RealTimeDbDataUtils.deleteArticlesFromServer(articles, session)
    }

    override fun getInitialWaitingTime(): Long {
        return INIT_WAIT_TIME_MS
    }

    companion object{
        private const val INIT_WAIT_TIME_MS = DateUtils.ONE_MINUTE_IN_MS/2
    }
}