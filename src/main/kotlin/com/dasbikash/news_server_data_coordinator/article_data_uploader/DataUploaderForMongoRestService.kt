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
import com.dasbikash.news_server_data_coordinator.utils.DateUtils
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import org.hibernate.Session

class DataUploaderForMongoRestService:DataUploader() {

    override fun getUploadDestinationInfo(): UploadDestinationInfo {
        return UploadDestinationInfo.MONGO_REST_SERVICE
    }

    override fun uploadArticles(articlesForUpload: List<Article>,session: Session): Boolean {
        try {
            LoggerUtils.logOnConsole("Going to upload data")
            TODO("Data uploader not implemented")
            LoggerUtils.logOnConsole("Upload successful")
            return true
        }catch (ex:Exception){
            println("Upload failure")
            ex.printStackTrace()
            return false
        }
    }

    override fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                                   newspapers: Collection<Newspaper>, pages: Collection<Page>,
                                   newsCategories: Collection<NewsCategory>) {
        TODO()
    }

    override fun addToServerUploadTimeLog() {
        TODO()
    }

    override fun nukeOldSettings() {
        TODO()
    }

    override fun deleteArticleFromServer(article: Article,session: Session): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteArticlesFromServer(articles: List<Article>, session: Session) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInitialWaitingTime(): Long {
        return INIT_WAIT_TIME_MS
    }

    companion object{
        private const val INIT_WAIT_TIME_MS = 10* DateUtils.ONE_MINUTE_IN_MS
    }
}