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
import org.hibernate.Session

class DataUploaderForFireStoreDb : DataUploader() {

    override fun getUploadDestinationInfo(): UploadDestinationInfo {
        return UploadDestinationInfo.FIRE_STORE_DB
    }

    override fun uploadArticles(articlesForUpload: List<Article>,session: Session): Boolean {
        FireStoreDataUtils.writeArticleData(articlesForUpload,session)
        LoggerUtils.logOnConsole("${articlesForUpload.size} articles uploaded to ${getUploadDestinationInfo().articleUploadTarget.name}")
        return true
    }

    override fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                                   newspapers: Collection<Newspaper>, pages: Collection<Page>,
                                   newsCategories: Collection<NewsCategory>) {
        FireStoreDataUtils.uploadNewSettings(languages, countries, newspapers, pages,newsCategories)
    }

    override fun addToServerUploadTimeLog() {
        FireStoreDataUtils.addToServerUploadTimeLog()
    }

    override fun nukeOldSettings() {
        FireStoreDataUtils.nukeAppSettings()
    }

    override fun deleteArticleFromServer(article: Article,session: Session): Boolean {
        return FireStoreDataUtils.deleteArticleFromServer(article)
    }
}