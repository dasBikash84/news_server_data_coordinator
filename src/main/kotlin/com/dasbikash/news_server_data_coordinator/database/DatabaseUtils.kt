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

package com.dasbikash.news_server_data_coordinator.database

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import com.dasbikash.news_server_data_coordinator.model.EntityClassNames
import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import org.hibernate.Session
import java.math.BigInteger

object DatabaseUtils {

    private val DB_WRITE_MAX_RETRY = 3

    fun runDbTransection(session: Session, operation: () -> Unit): Boolean {

        var retryLimit = DB_WRITE_MAX_RETRY;

        var exception: Exception

        do {
            try {
                if (!session.transaction.isActive) {
                    session.beginTransaction()
                }
                operation()
                session.transaction.commit()
                return true
            } catch (ex: Exception) {
                ex.printStackTrace()
                exception = ex
            }
        } while (--retryLimit > 0)

        val stackTrace = mutableListOf<StackTraceElement>()
        exception.stackTrace.toCollection(stackTrace)

        try {
            if (!session.transaction.isActive) {
                session.beginTransaction()
            }
            LoggerUtils.logMessage("Message: ${exception.message} Cause: ${exception.cause?.message} StackTrace: ${stackTrace}", session)
            session.transaction.commit()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return false
    }

    fun getLanguageMap(session: Session): Map<String, Language> {
        val hql = "FROM ${EntityClassNames.LANGUAGE}"
        val query = session.createQuery(hql, Language::class.java)
        val languageMapFromDb = mutableMapOf<String, Language>()

        (query.list() as List<Language>).asSequence().forEach {
            languageMapFromDb.put(it.id, it)
        }
        return languageMapFromDb
    }

    fun getCountriesMap(session: Session): Map<String, Country> {
        val hql = "FROM ${EntityClassNames.COUNTRY}"
        val query = session.createQuery(hql, Country::class.java)
        val countriesMap = mutableMapOf<String, Country>()
        (query.list() as List<Country>).asSequence()
                .forEach {
                    countriesMap.put(it.name, it)
                }
        return countriesMap
    }

    fun getActiveNewspaperMap(session: Session): Map<String, Newspaper> {
        val hql = "FROM ${EntityClassNames.NEWSPAPER} where active=true"
        val query = session.createQuery(hql, Newspaper::class.java)
        val newspaperMap = mutableMapOf<String, Newspaper>()
        (query.list() as List<Newspaper>).asSequence()
                .forEach {
                    newspaperMap.put(it.id, it)
                }
        return newspaperMap
    }

    fun getNewspaperMap(session: Session): Map<String, Newspaper> {
        val hql = "FROM ${EntityClassNames.NEWSPAPER}"
        val query = session.createQuery(hql, Newspaper::class.java)
        val newspaperMap = mutableMapOf<String, Newspaper>()
        (query.list() as List<Newspaper>).asSequence()
                .forEach {
                    newspaperMap.put(it.id, it)
                }
        return newspaperMap
    }

    fun getActivePageMap(session: Session): Map<String, Page> {
        val hql = "FROM ${EntityClassNames.PAGE} where active=true"
        val query = session.createQuery(hql, Page::class.java)
        val pageMap = mutableMapOf<String, Page>()
        (query.list() as List<Page>).asSequence()
                .filter {
                    it.newspaper!!.active
                }
                .forEach {
                    pageMap.put(it.id, it)
                }
        return pageMap
    }

    fun getPageMap(session: Session): Map<String, Page> {
        val hql = "FROM ${EntityClassNames.PAGE}"
        val query = session.createQuery(hql, Page::class.java)
        val pageMap = mutableMapOf<String, Page>()
        (query.list() as List<Page>).asSequence()
                .filter {
                    it.newspaper!!.active
                }
                .forEach {
                    pageMap.put(it.id, it)
                }
        return pageMap
    }

    fun getPageGroups(session: Session): List<PageGroup> {
        val hql = "FROM ${EntityClassNames.PAGE_GROUP}"
        val query = session.createQuery(hql, PageGroup::class.java)
        return query.list() as List<PageGroup>
    }

    fun findArticleById(session: Session, id: String): Article? {
        val hql = "FROM ${EntityClassNames.ARTICLE} where id='${id}'"
        val query = session.createQuery(hql, Article::class.java)
        val resultList = query.list() as List<Article>
        if (resultList.size == 1) {
            return resultList.get(0)
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    fun findLatestArticleForPage(session: Session, page: Page): Article? {
        val sql = "SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME} where pageId='${page.id}' order by publicationTime DESC LIMIT 1"
        val query = session.createNativeQuery(sql, Article::class.java)
        val resultList = query.resultList as List<Article>
        if (resultList.size == 1) {
            return resultList.get(0)
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    fun findOldestArticleForPage(session: Session, page: Page): Article? {
        val sql = "SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME} where pageId='${page.id}' order by publicationTime ASC LIMIT 1"
        val query = session.createNativeQuery(sql, Article::class.java)
        val resultList = query.resultList as List<Article>
        if (resultList.size == 1) {
            return resultList.get(0)
        }
        return null
    }

    fun findPageById(session: Session, id: String): Page? {
        val hql = "FROM ${EntityClassNames.PAGE} where id='${id}'"
        val query = session.createQuery(hql, Page::class.java)
        val resultList = query.list() as List<Page>
        if (resultList.size == 1) {
            return resultList.get(0)
        }
        return null
    }

    fun findNewspaperById(session: Session, id: String): Newspaper? {
        val hql = "FROM ${EntityClassNames.NEWSPAPER} where id='${id}'"
        val query = session.createQuery(hql, Newspaper::class.java)
        val resultList = query.list() as List<Newspaper>
        if (resultList.size == 1) {
            return resultList.get(0)
        }
        return null
    }

    private fun findSettingsUploadLogByTarget(session: Session, articleUploadTarget: ArticleUploadTarget): List<SettingsUploadLog> {
        val hql = "FROM ${EntityClassNames.SETTINGS_UPLOAD_LOG} where uploadTarget ='${articleUploadTarget}'"
        val query = session.createQuery(hql, SettingsUploadLog::class.java)
        return query.list() as List<SettingsUploadLog>
    }

    fun getLastSettingsUploadLogByTarget(session: Session, articleUploadTarget: ArticleUploadTarget)
            : SettingsUploadLog? {
        val settingsUploadLogList = findSettingsUploadLogByTarget(session, articleUploadTarget)
        if (settingsUploadLogList.isNotEmpty()) {
            return settingsUploadLogList.sortedBy {
                        it.uploadTime
                    }.last()
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    fun getLastSettingsUpdateLog(session: Session): SettingsUpdateLog {
        val hql = "FROM ${EntityClassNames.SETTINGS_UPDATE_LOG} order by updateTime asc"
        val query = session.createQuery(hql, SettingsUpdateLog::class.java)
        return (query.list() as List<SettingsUpdateLog>).last()
    }

    fun getArticleUploaderStatus(session: Session,articleUploadTarget: ArticleUploadTarget):Boolean{
        val hql = "FROM ${EntityClassNames.ARTICLE_UPLOADER_STATUS_CHANGE_LOG} where " +
                            "articleDataUploaderTarget='${articleUploadTarget}' order by created desc"
        val query = session.createQuery(hql, ArticleUploaderStatusChangeLog::class.java)
        val articleUploaderStatusChangeLogList = query.list() as List<ArticleUploaderStatusChangeLog>
        if (articleUploaderStatusChangeLogList.size>0){
            return articleUploaderStatusChangeLogList.first().status == TwoStateStatus.ON
        }
        return false
    }

    fun getArticleDeleteRequests(session: Session): List<ArticleDeleteRequest> {
        val hql = "FROM ${EntityClassNames.ARTICLE_DELETE_REQUEST}"
        val query = session.createQuery(hql, ArticleDeleteRequest::class.java)
        return query.list() as List<ArticleDeleteRequest>
    }

    fun getArticleDeleteRequestCount(session: Session): Int {
        val hql = "SELECT COUNT(*) FROM ${EntityClassNames.ARTICLE_DELETE_REQUEST}"
        val query = session.createQuery(hql)
        return (query.singleResult as Long).toInt()
    }

    fun getArticleDeleteRequestServingLogCount(session: Session): Int {
        val hql = "SELECT COUNT(*) FROM ${EntityClassNames.ARTICLE_DELETE_REQUEST_SERVING_LOG}"
        val query = session.createQuery(hql)
        return (query.singleResult as Long).toInt()
    }

    fun getArticleDeleteRequestServingLogCountForTarget(session: Session,articleUploadTarget: ArticleUploadTarget): Int {
        val sqlString = "SELECT COUNT(*) FROM ${DatabaseTableNames.ARTICLE_DELETE_REQUEST_SERVING_LOG_TABLE_NAME} WHERE articleUploadTarget='${articleUploadTarget.name}'"
        val query = session.createNativeQuery(sqlString)
        return (query.singleResult as BigInteger).toInt()
    }

    fun getArticleDeleteRequestServingLogsForTarget(session: Session,articleUploadTarget: ArticleUploadTarget): List<ArticleDeleteRequestServingLog> {
        val sqlString = "SELECT * FROM ${DatabaseTableNames.ARTICLE_DELETE_REQUEST_SERVING_LOG_TABLE_NAME} WHERE articleUploadTarget='${articleUploadTarget.name}'"
        val query = session.createNativeQuery(sqlString,ArticleDeleteRequestServingLog::class.java)
        return query.resultList as List<ArticleDeleteRequestServingLog>
    }

    fun getArticlesForDeletion(session: Session, page: Page, deleteRequestCount: Int, articleUploadTarget: ArticleUploadTarget): List<Article> {
        val sqlStringBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}").append(" WHERE pageId='${page.id}' AND")
        when(articleUploadTarget){
            ArticleUploadTarget.REAL_TIME_DB -> sqlStringBuilder.append( " upOnFirebaseDb=1 AND deletedFromFirebaseDb=0")
            ArticleUploadTarget.FIRE_STORE_DB -> sqlStringBuilder.append( " upOnFireStore=1 AND deletedFromFireStore=0")
            ArticleUploadTarget.MONGO_REST_SERVICE -> sqlStringBuilder.append( " upOnMongoRest=1 AND deletedFromMongoRest=0")
        }
        sqlStringBuilder.append(" ORDER BY publicationTime asc limit ${deleteRequestCount}")
        println(sqlStringBuilder.toString())
        val query = session.createNativeQuery(sqlStringBuilder.toString(),Article::class.java)
        return query.resultList as List<Article>
    }

    fun markArticleAsDeletedFromDataStore(session: Session,article: Article,articleUploadTarget: ArticleUploadTarget){
        val sqlStringBuilder = StringBuilder("UPDATE ${DatabaseTableNames.ARTICLE_TABLE_NAME}").append(" SET")
        when(articleUploadTarget){
            ArticleUploadTarget.REAL_TIME_DB -> sqlStringBuilder.append( " deletedFromFirebaseDb=1")
            ArticleUploadTarget.FIRE_STORE_DB -> sqlStringBuilder.append( " deletedFromFireStore=1")
            ArticleUploadTarget.MONGO_REST_SERVICE -> sqlStringBuilder.append( " deletedFromMongoRest=1")
        }
        sqlStringBuilder.append(" WHERE id='${article.id}'")
        println(sqlStringBuilder.toString())
        val query = session.createNativeQuery(sqlStringBuilder.toString())
        runDbTransection(session) {
            query.executeUpdate()
        }
    }

}