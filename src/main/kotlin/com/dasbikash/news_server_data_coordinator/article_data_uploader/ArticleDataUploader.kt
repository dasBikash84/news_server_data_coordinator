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
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.*
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import org.hibernate.Session
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*


abstract class ArticleDataUploader:Thread() {
    private val MAX_ARTICLE_INVALID_AGE_ERROR_MESSAGE = "Max article age must be positive"
    private val MAX_ARTICLE_COUNT_INVALID_ERROR_MESSAGE = "Max article count for upload must be positive"

    private val WAITING_TIME_FOR_NEW_ARTICLES_FOR_UPLOAD_MS = 10*60*60*1000L // 10 mins
    private val WAITING_TIME_BETWEEN_ITERATION = 5*1000L //5 secs

    private val SQL_DATE_FORMAT = "yyyy-MM-dd"
    private val sqlDateFormatter = SimpleDateFormat(SQL_DATE_FORMAT)


    abstract protected fun getUploadDestinationInfo():UploadDestinationInfo
    abstract protected fun getMaxArticleAgeInDays():Int //Must be >=0
    abstract protected fun uploadArticles(articlesForUpload: List<Article>):Boolean
    abstract protected fun maxArticleCountForUpload():Int // Must be >=0
    abstract protected fun nukeOldSettings()
    abstract protected fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                                             newspapers: Collection<Newspaper>, pages: Collection<Page>)
    abstract protected fun addToServerUploadTimeLog()

    private fun getSqlForArticleFetch():String{
        if (maxArticleCountForUpload()<0){
            throw IllegalArgumentException(MAX_ARTICLE_COUNT_INVALID_ERROR_MESSAGE)
        }
        val sqlBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                                        .append(" WHERE ${Article.PUBLICATION_TIME_COLUMN_NAME} > '")
        sqlBuilder
                .append(getMinArticleDateString())
                .append("' AND ${getUploadDestinationInfo().flagName}=0")
                .append(" ORDER BY ${Article.COLUMN_NAME_FOR_ORDER_BY} DESC")
                .append(" LIMIT ${maxArticleCountForUpload()}")
        return sqlBuilder.toString()
    }

    private fun getMinArticleDateString(): String {
        if (getMaxArticleAgeInDays() < 0){
            throw IllegalArgumentException(MAX_ARTICLE_INVALID_AGE_ERROR_MESSAGE)
        }
        val today = Calendar.getInstance()
        today.add(Calendar.DAY_OF_YEAR,-1*getMaxArticleAgeInDays())
        return sqlDateFormatter.format(today.time)
    }

    private fun getArticlesForUpload(session:Session): List<Article> {
        val nativeSql = getSqlForArticleFetch()
        println("SqlForArticleFetch: ${nativeSql}")
        return session.createNativeQuery(nativeSql, Article::class.java).resultList as List<Article>
    }

    private fun getSqlToMarkUploadedArticle(article: Article):String{
        return "UPDATE ${DatabaseTableNames.ARTICLE_TABLE_NAME} SET ${getUploadDestinationInfo().flagName}=1 WHERE id='${article.id}'"
    }

    private fun markArticlesAsUploaded(articlesForUpload: List<Article>,session: Session) {
        var flag = false
        articlesForUpload.asSequence()
                .forEach {
                    val sql = getSqlToMarkUploadedArticle(it)
                    if (!flag) {
                        println("sql to ArticlesAsUploaded: ${sql}")
                        flag=true
                    }
                    DatabaseUtils.runDbTransection(session) {
                        session.createNativeQuery(sql).executeUpdate()
                    }
                }
    }

    private fun checkIfSettingsModified(session:Session):Boolean{
        val settingsUpdateLog = DatabaseUtils.getLastSettingsUpdateLog(session)
        val settingsUploadLog = DatabaseUtils
                                                    .getLastSettingsUploadLogByTarget(session, getUploadDestinationInfo().articleUploadTarget)
        println(settingsUpdateLog)
        println(settingsUploadLog)
        if (settingsUploadLog !=null){
            if (settingsUploadLog.uploadTime > settingsUpdateLog.updateTime){
                return false
            }
        }
        return true
    }

    private fun addSettingsUpdateLog(session: Session){
        DatabaseUtils.runDbTransection(session) {
            session.save(SettingsUploadLog(uploadTarget = getUploadDestinationInfo().articleUploadTarget))
        }
    }
    private fun uploadSettingsToServer(session: Session){
        val languages = DatabaseUtils.getLanguageMap(session).values
        val countries = DatabaseUtils.getCountriesMap(session).values
        val newspapers = DatabaseUtils.getNewspaperMap(session).values
        val pages = DatabaseUtils.getPageMap(session).values
        if (languages.isEmpty() || countries.isEmpty() || newspapers.isEmpty() || pages.isEmpty()) {
            throw IllegalStateException("Basic app settings not found.")
        }
        nukeOldSettings()
        uploadNewSettings(languages, countries, newspapers, pages)
        addToServerUploadTimeLog()
        addSettingsUpdateLog(session)
    }

    override fun run() {
        do {
            val session = DbSessionManager.getNewSession()

            if (checkIfSettingsModified(session)) {
                uploadSettingsToServer(session)
            }

            val articlesForUpload = getArticlesForUpload(session)
            println("articlesForUpload.size: ${articlesForUpload.size}")
            if (articlesForUpload.size>0){
                if (uploadArticles(articlesForUpload)){
                    markArticlesAsUploaded(articlesForUpload,session)
                    LoggerUtils.logArticleUploadHistory(session,articlesForUpload,getUploadDestinationInfo())
                }
                try {
                    session.close()
                    sleep(WAITING_TIME_BETWEEN_ITERATION)
                }catch (ex:Exception){
                    ex.printStackTrace()
                }
            }else{
                try {
                    session.close()
                    sleep(WAITING_TIME_FOR_NEW_ARTICLES_FOR_UPLOAD_MS)
                }catch (ex:Exception){
                    ex.printStackTrace()
                }
            }
        }while (true)
    }
}