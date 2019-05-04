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

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.firebase.*
import com.dasbikash.news_server_data_coordinator.model.*
import com.google.api.core.ApiFuture
import com.google.firebase.database.ServerValue
import org.hibernate.Session

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

    override fun uploadSettings(session: Session) {
        val languages = DatabaseUtils.getLanguageMap(session).values
        val countries = DatabaseUtils.getCountriesMap(session).values
        val newspapers = DatabaseUtils.getNewspaperMap(session).values
        val pages = DatabaseUtils.getPageMap(session).values
        if (languages.isEmpty() || countries.isEmpty() || newspapers.isEmpty() || pages.isEmpty()) {
            throw IllegalArgumentException()
        }
        nukeOldSettings()
        uploadNewSettings(languages, countries, newspapers, pages)
        addUploadTime()
    }

    override fun insertLog(session: Session) {
        DatabaseUtils.runDbTransection(session) {
            session.save(SettingsUploadLog(uploadTarget = getUploadDestinationInfo().articleUploadTarget))
        }
    }

    private fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                                  newspapers: Collection<Newspaper>, pages: Collection<Page>) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val listOfFuture = mutableListOf<ApiFuture<Void>>()
        languages.asSequence().forEach {
            listOfFuture.add(FirebaseDbRefUtils.getLanguagesRef().child(it.id).setValueAsync(LanguageForFB.getFromLanguage(it)))
        }
        countries.asSequence().forEach {
            listOfFuture.add(FirebaseDbRefUtils.getCountriesRef().child(it.name).setValueAsync(CountryForFB.getFromCountry(it)))
        }
        newspapers.asSequence().forEach {
            listOfFuture.add(FirebaseDbRefUtils.getNewspapersRef().child(it.id).setValueAsync(NewspaperForFB.getFromNewspaper(it)))
        }
        pages.asSequence().forEach {
            listOfFuture.add(FirebaseDbRefUtils.getPagesRef().child(it.id).setValueAsync(PageForFB.getFromPage(it)))
        }

        listOfFuture.asSequence().forEach {
            while (!it.isDone) {
            }
        }
    }

    private fun addUploadTime() {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val task = FirebaseDbRefUtils.getSettingsUpdateTimeRef().push().setValueAsync(ServerValue.TIMESTAMP)
        while (!task.isDone) {
        }
    }

    private fun nukeOldSettings() {
//        TODO()
        val listOfFuture = mutableListOf<ApiFuture<Void>>()
        listOfFuture.add(FirebaseDbRefUtils.getPagesRef().setValueAsync(null))
        listOfFuture.add(FirebaseDbRefUtils.getNewspapersRef().setValueAsync(null))
        listOfFuture.add(FirebaseDbRefUtils.getCountriesRef().setValueAsync(null))
        listOfFuture.add(FirebaseDbRefUtils.getLanguagesRef().setValueAsync(null))
        listOfFuture.asSequence().forEach {
            while (!it.isDone) {
            }
        }
    }

    companion object {
        private const val MAX_ARTICLE_AGE_DAYS = 30
        private const val MAX_ARTICLE_COUNT_FOR_UPLOAD = 5
    }
}