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
import org.hibernate.Session

class DataUploaderForRealTimeDb : DataUploader() {

    override fun getUploadDestinationInfo(): UploadDestinationInfo {
        return UploadDestinationInfo.REAL_TIME_DB
    }

    override fun getMaxArticleAgeInDays(): Int {
        return MAX_ARTICLE_AGE_DAYS
    }

    override fun uploadArticles(articlesForUpload: List<Article>): Boolean {
        try {
            RealTimeDbDataUtils.writeArticleData(articlesForUpload)
            println("${articlesForUpload.size} articles uploaded to ${getUploadDestinationInfo().articleUploadTarget.name}")
            return true
        } catch (ex: Exception) {
            println("Article upload failure to ${getUploadDestinationInfo().articleUploadTarget}")
            ex.printStackTrace()
            return false
        }
    }

    override fun maxArticleCountForUpload(): Int {
        return MAX_ARTICLE_COUNT_FOR_UPLOAD
    }

    override fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                                   newspapers: Collection<Newspaper>, pages: Collection<Page>,
                                   pageGroups:Collection<PageGroup>) {
        RealTimeDbDataUtils.uploadNewSettings(languages, countries, newspapers, pages,pageGroups)
    }

    override fun addToServerUploadTimeLog() {
        RealTimeDbDataUtils.addToServerUploadTimeLog()
    }

    override fun nukeOldSettings() {
        RealTimeDbDataUtils.nukeAppSettings()
    }

    override fun serveArticleDeleteRequest(session: Session, articleDeleteRequest: ArticleDeleteRequest) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private const val MAX_ARTICLE_AGE_DAYS = 30
        private const val MAX_ARTICLE_COUNT_FOR_UPLOAD = 400
    }
}