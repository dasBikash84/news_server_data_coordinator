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

import com.dasbikash.news_server_data_coordinator.article_data_uploader.UploadDestinationInfo
import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import com.dasbikash.news_server_data_coordinator.model.EntityClassNames
import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.dasbikash.news_server_data_coordinator.utils.DateUtils
import org.hibernate.Session
import java.util.*

object DatabaseUtils {

    private val DB_WRITE_MAX_RETRY = 3

    fun runDbTransections(session: Session, operations: List<() -> Unit>): Boolean {

        var retryLimit = DB_WRITE_MAX_RETRY;

        var exception: Exception

        do {
            try {
                if (!session.transaction.isActive) {
                    session.beginTransaction()
                }
                operations.asSequence().forEach { it() }
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
//            LoggerUtils.logOnDb("Message: ${exception.message} Cause: ${exception.cause?.message} StackTrace: ${stackTrace}", session)
            session.transaction.commit()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return false
    }

    fun runDbTransection(session: Session, operation: () -> Unit) =
        runDbTransections(session, listOf(operation))

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

    fun getPageMapForAll(session: Session): Map<String, Page> {
        val hql = "FROM ${EntityClassNames.PAGE}"
        val query = session.createQuery(hql, Page::class.java)
        val pageMap = mutableMapOf<String, Page>()
        (query.list() as List<Page>).asSequence()
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

    fun getAllPages(session: Session): List<Page> {
        val hql = "FROM ${EntityClassNames.PAGE}"
        val query = session.createQuery(hql, Page::class.java)
        return query.list()
    }

    fun getNewsCategoryMap(session: Session): Map<String,NewsCategory> {
        val hql = "FROM ${EntityClassNames.NEWS_CATERORIES}"
        val query = session.createQuery(hql, NewsCategory::class.java)
        val newsCategoryMap = mutableMapOf<String,NewsCategory>()
        query.list().asSequence().forEach { newsCategoryMap.put(it.id,it) }
        return newsCategoryMap.toMap()
    }

    fun getNewsCategoryEntryMap(session: Session): Map<Int,NewsCategoryEntry> {
        val hql = "FROM ${EntityClassNames.NEWS_CATERORY_ENTRIES}"
        val query = session.createQuery(hql, NewsCategoryEntry::class.java)
        val newsCategoryEntryMap = mutableMapOf<Int,NewsCategoryEntry>()
        query.list().asSequence().forEach { newsCategoryEntryMap.put(it.id!!,it) }
        return newsCategoryEntryMap.toMap()
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

    fun getArticleUploaderStatus(session: Session, articleUploadTarget: ArticleUploadTarget): Boolean {
        val hql = "FROM ${EntityClassNames.ARTICLE_UPLOADER_STATUS_CHANGE_LOG} where " +
                "articleDataUploaderTarget='${articleUploadTarget}' order by created desc"
        val query = session.createQuery(hql, ArticleUploaderStatusChangeLog::class.java)
        val articleUploaderStatusChangeLogList = query.list() as List<ArticleUploaderStatusChangeLog>
        if (articleUploaderStatusChangeLogList.size > 0) {
            return articleUploaderStatusChangeLogList.first().status == TwoStateStatus.ON
        }
        return false
    }

    fun getArticleDeleteRequests(session: Session, articleUploadTarget: ArticleUploadTarget): List<ArticleDeleteRequest> {
        val hql = "FROM ${EntityClassNames.ARTICLE_DELETE_REQUEST} WHERE served=false AND articleUploadTarget='${articleUploadTarget.name}'"
        val query = session.createQuery(hql, ArticleDeleteRequest::class.java)
        return query.list() as List<ArticleDeleteRequest>
    }

    fun getArticlesForDeletion(session: Session, page: Page, deleteRequestCount: Int, uploadDestinationInfo: UploadDestinationInfo): List<Article> {
        val sqlStringBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE pageId='${page.id}' ")
                .append(" AND ${uploadDestinationInfo.uploadFlagName} = 1")
                .append(" AND ${uploadDestinationInfo.deleteFlagName} = 0")
                .append(" ORDER BY publicationTime asc")
                .append(" limit ${deleteRequestCount}")

//        LoggerUtils.logOnConsole(sqlStringBuilder.toString())
        val query = session.createNativeQuery(sqlStringBuilder.toString(), Article::class.java)
        try {
            return query.resultList as List<Article>
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return emptyList()
    }

    fun markArticleAsDeletedFromDataStore(session: Session, article: Article, uploadDestinationInfo: UploadDestinationInfo) {
        val sqlStringBuilder = StringBuilder("UPDATE ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" SET ${uploadDestinationInfo.deleteFlagName} = 1")
                .append(" WHERE id='${article.id}'")
        val query = session.createNativeQuery(sqlStringBuilder.toString())
        runDbTransection(session) {
            query.executeUpdate()
        }
    }

    fun getArticleDownloadLogWithNullPageId(session: Session, count: Int): List<ArticleDownloadLog> {
        val sql = "SELECT * FROM ${DatabaseTableNames.ARTICLE_DOWNLOAD_LOG_TABLE_NAME} WHERE " +
                "pageId is null order by created desc limit ${count}"
//        LoggerUtils.logOnConsole(sql)
        try {
            return session.createNativeQuery(sql, ArticleDownloadLog::class.java).resultList as List<ArticleDownloadLog>
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return emptyList()
    }

    fun getArticleDownloadLog(session: Session, count: Int): List<ArticleDownloadLog> {
        val sql = "SELECT * FROM ${DatabaseTableNames.ARTICLE_DOWNLOAD_LOG_TABLE_NAME} limit ${count}"
        try {
            return session.createNativeQuery(sql, ArticleDownloadLog::class.java).resultList as List<ArticleDownloadLog>
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return emptyList()
    }

    fun getArticleDownloadCountForPageOfYesterday(session: Session, page: Page, today: Date): Int {
        val yesterday = DateUtils.getYesterDay(today)
        return getArticleDownloadCountForPageBetweenTwoDates(page, yesterday, today, session)
    }

    fun getArticleDownloadCountForPageOfLastWeek(session: Session, page: Page, thisWeekFirstDay: Date): Int {
        val lastWeekFirstDay = DateUtils.getLastWeekSameDay(thisWeekFirstDay)
        return getArticleDownloadCountForPageBetweenTwoDates(page, lastWeekFirstDay, thisWeekFirstDay, session)
    }

    fun getArticleDownloadCountForPageOfLastMonth(session: Session, page: Page, anyDayOfMonth: Date): Int {
        val firstDayOfMonth = DateUtils.getFirstDayOfMonth(anyDayOfMonth)
        val firstDayOfLastMonth = DateUtils.getFirstDayOfLastMonth(anyDayOfMonth)

        return getArticleDownloadCountForPageBetweenTwoDates(page, firstDayOfLastMonth, firstDayOfMonth, session)
    }

    private fun getArticleDownloadCountForPageBetweenTwoDates(page: Page, startDate: Date, firstDayOfMonth: Date, session: Session): Int {
        val sqlBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_DOWNLOAD_LOG_TABLE_NAME}")
                .append(" WHERE pageId='${page.id}' ")
                .append("AND created>='${DateUtils.getDateStringForDb(startDate)}'")
                .append("AND created<'${DateUtils.getDateStringForDb(firstDayOfMonth)}'")
//        LoggerUtils.logOnConsole(sqlBuilder.toString())
        try {
            val articleDownloadLogs =
                    session.createNativeQuery(sqlBuilder.toString(), ArticleDownloadLog::class.java).resultList as List<ArticleDownloadLog>
            var articleDownloadCount = 0
            articleDownloadLogs.asSequence().forEach {
                articleDownloadCount += it.articleCount//getArticleCount()
            }
            return articleDownloadCount
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return 0
    }

    fun getArticleUploadCountForTargetOfYesterday(session: Session, articleUploadTarget: ArticleUploadTarget, today: Date): Int {
        val yesterday = DateUtils.getYesterDay(today)
        return getArticleUploadCountForTargetBetweenTwoDates(articleUploadTarget, yesterday, today, session)
    }

    fun getArticleUploadCountForTargetOfLastWeek(session: Session, articleUploadTarget: ArticleUploadTarget, thisWeekFirstDay: Date): Int {
        val lastWeekFirstDay = DateUtils.getLastWeekSameDay(thisWeekFirstDay)
        return getArticleUploadCountForTargetBetweenTwoDates(articleUploadTarget, lastWeekFirstDay, thisWeekFirstDay, session)
    }

    fun getArticleUploadCountForTargetOfLastMonth(session: Session, articleUploadTarget: ArticleUploadTarget, anyDayOfMonth: Date): Int {
        val firstDayOfMonth = DateUtils.getFirstDayOfMonth(anyDayOfMonth)
        val firstDayOfLastMonth = DateUtils.getFirstDayOfLastMonth(anyDayOfMonth)
        return getArticleUploadCountForTargetBetweenTwoDates(articleUploadTarget, firstDayOfLastMonth, firstDayOfMonth, session)
    }

    fun getArticleUploadCountForTargetFromBeginning(session: Session, articleUploadTarget: ArticleUploadTarget): Int {

        val uploadDestinationInfo =
                UploadDestinationInfo.values().find { it.articleUploadTarget == articleUploadTarget }!!


        val sqlBuilder = StringBuilder("SELECT count(*) FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE ${uploadDestinationInfo.uploadFlagName}=1")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())

        try {
            val result = session.createNativeQuery(sqlBuilder.toString()).list() as List<Int>
            if (result.size == 1) {
                return result.get(0)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return 0
    }

    private fun getArticleUploadCountForTargetBetweenTwoDates(articleUploadTarget: ArticleUploadTarget, startDate: Date, endDate: Date, session: Session): Int {

        val sqlBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_UPLOAD_LOG_TABLE_NAME}")
                .append(" WHERE uploadTarget='${articleUploadTarget.name}' ")
                .append("AND created>='${DateUtils.getDateStringForDb(startDate)}'")
                .append("AND created<'${DateUtils.getDateStringForDb(endDate)}'")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())
        try {
            val articleUploadLogs =
                    session.createNativeQuery(sqlBuilder.toString(), ArticleUploadLog::class.java).resultList as List<ArticleUploadLog>
            var articleUploadCount = 0
            articleUploadLogs.asSequence().forEach {
                articleUploadCount += it.articleCount//getArticleUpCount()
            }
            return articleUploadCount
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return 0
    }

    fun getArticleCountForPage(session: Session, page: Page): Int {
        val sql = "SELECT COUNT(*) FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME} WHERE pageId='${page.id}'"
        try {
            val result = session.createNativeQuery(sql).list() as List<Int>
            if (result.size == 1) {
                return result.get(0)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return 0
    }

    fun getArticleDeletionCountFromAllUploaderTargetsForPage(session: Session, page: Page): Map<ArticleUploadTarget, Int> {

        val targetArticleDeletionCountMap = mutableMapOf<ArticleUploadTarget, Int>()

        getArticleDeletionCountFromUploaderTargetForPage(session, ArticleUploadTarget.REAL_TIME_DB, page).apply {
            targetArticleDeletionCountMap.put(ArticleUploadTarget.REAL_TIME_DB, this)
        }
        getArticleDeletionCountFromUploaderTargetForPage(session, ArticleUploadTarget.FIRE_STORE_DB, page).apply {
            targetArticleDeletionCountMap.put(ArticleUploadTarget.FIRE_STORE_DB, this)
        }
        getArticleDeletionCountFromUploaderTargetForPage(session, ArticleUploadTarget.MONGO_REST_SERVICE, page).apply {
            targetArticleDeletionCountMap.put(ArticleUploadTarget.MONGO_REST_SERVICE, this)
        }
        return targetArticleDeletionCountMap.toMap()
    }

    private fun getArticleDeletionCountFromUploaderTargetForPage(session: Session, articleUploadTarget: ArticleUploadTarget, page: Page): Int {

        val uploadDestinationInfo =
                UploadDestinationInfo.values().find { it.articleUploadTarget == articleUploadTarget }!!


        val sqlBuilder = StringBuilder("SELECT count(*) FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE pageId='${page.id}' ")
                .append(" AND ${uploadDestinationInfo.deleteFlagName}=1")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())

        try {
            val result = session.createNativeQuery(sqlBuilder.toString()).list() as List<Int>
            if (result.size == 1) {
                return result.get(0)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return 0

    }

    fun getArticleDeletionCountFromUploaderTarget(session: Session, articleUploadTarget: ArticleUploadTarget): Int {

        val uploadDestinationInfo =
                UploadDestinationInfo.values().find { it.articleUploadTarget == articleUploadTarget }!!


        val sqlBuilder = StringBuilder("SELECT count(*) FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE ${uploadDestinationInfo.deleteFlagName}=1")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())
        try {
            val result = session.createNativeQuery(sqlBuilder.toString()).list() as List<Int>
            if (result.size == 1) {
                return result.get(0)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return 0

    }

    fun getLastDeletionTaskLogForTarget(session: Session, articleUploadTarget: ArticleUploadTarget): DailyDeletionTaskLog? {

        val sqlBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.DAILY_DELETION_TASK_LOG_TABLE_NAME}")
                .append(" WHERE uploadTarget='${articleUploadTarget.name}'")
                .append(" ORDER BY created DESC")
                .append(" limit 1")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())

        try {
            val dailyDeletionTaskLogs =
                    session.createNativeQuery(sqlBuilder.toString(), DailyDeletionTaskLog::class.java).resultList as List<DailyDeletionTaskLog>

            if (dailyDeletionTaskLogs.size == 1) {
                return dailyDeletionTaskLogs.get(0)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }

        return null
    }

    fun getArticleCountInTargetForPage(session: Session, page: Page, uploadDestinationInfo: UploadDestinationInfo): Int {
        val sqlBuilder = StringBuilder("SELECT COUNT(*) FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE pageId='${page.id}'")
                .append(" AND ${uploadDestinationInfo.uploadFlagName} = 1")
                .append(" AND ${uploadDestinationInfo.deleteFlagName} = 0")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())

        try {
            val result = session.createNativeQuery(sqlBuilder.toString()).list() as List<Int>
            if (result.size == 1) {
                return result.get(0)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return 0
    }

    fun getAllRestrictedSearchKeyWord(session: Session): List<RestrictedSearchKeyWord> {
        val hql = "FROM ${EntityClassNames.RESTRICTED_SEARCH_KEY_WORD}"
        val query =
                session.createQuery(hql, RestrictedSearchKeyWord::class.java)
        try {
            return query.list() as List<RestrictedSearchKeyWord>
        } catch (ex: Exception) {
            return emptyList()
        }
    }

    fun getUnProcessedArticlesForSearchResult(session: Session, limit: Int = 100): List<Article> {
        val sqlBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE")
                .append(" processedForSearchResult=false")
                .append(" AND")
                .append(" ((upOnFirebaseDb=1 AND deletedFromFirebaseDb=0) OR (upOnFireStore=1 AND deletedFromFireStore=0))")
                .append(" limit ${limit}")

        val query = session.createNativeQuery(sqlBuilder.toString(), Article::class.java)
        try {
            return query.resultList as List<Article>
        } catch (ex: Exception) {
            return emptyList()
        }
    }

    fun getUnProcessedDeletedArticlesForSearchResult(session: Session, limit: Int = 100): List<Article> {
        val sqlBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE processedForSearchResult = 1")
                .append(" AND deletedFromFirebaseDb=1")
                .append(" AND deletedFromFireStore=1")
                .append(" AND deletedProcessedForSearchResult = 0")
                .append(" limit ${limit}")

        val query = session.createNativeQuery(sqlBuilder.toString(), Article::class.java)
        try {
            return query.resultList as List<Article>
        } catch (ex: Exception) {
            return emptyList()
        }
    }

    fun checkIfArticleDeleted(session: Session, articleId: String): Boolean {
        val sqlBuilder = StringBuilder("SELECT Count(*) FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE id='${articleId}'")
                .append(" AND processedForSearchResult=1")
                .append(" AND deletedFromFirebaseDb=1")
                .append(" AND deletedFromFireStore=1")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())

        try {
            val result = session.createNativeQuery(sqlBuilder.toString()).list() as List<Int>
            if (result.size == 1) {
                return result.get(0) == 1
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return false
    }

    fun getNewKeyWordSearchResults(session: Session, limit: Int = 100): List<KeyWordSearchResult> {
        val sqlBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.KEY_WORD_SERACH_RESULT_TABLE_NAME}")
                .append(" WHERE")
                .append(" lastUploadedOnFireBaseDb IS NULL")
                .append(" OR lastUploadedOnFireBaseDb < modified")
                .append(" limit ${limit}")

        val query = session.createNativeQuery(sqlBuilder.toString(), KeyWordSearchResult::class.java)
        try {
            return query.resultList as List<KeyWordSearchResult>
        } catch (ex: Exception) {
            return emptyList()
        }
    }

    fun getSearchKeyWords(session: Session): List<String> {
        val sqlBuilder = StringBuilder("SELECT keyWord FROM ${DatabaseTableNames.KEY_WORD_SERACH_RESULT_TABLE_NAME}")
                .append(" WHERE")
                .append(" lastUploadedOnFireBaseDb IS NOT NULL")
        println(sqlBuilder.toString())
        val query = session.createNativeQuery(sqlBuilder.toString())
        try {
            return query.list() as List<String>
        } catch (ex: Exception) {
            ex.printStackTrace()
            return emptyList()
        }
    }


    fun getLastArticleSearchResultUploaderLog(session: Session): ArticleSearchResultUploaderLog? {

        val sqlBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_SEARCH_RESULT_UPLOADER_LOG_TABLE_NAME}")
                .append(" ORDER BY created DESC")
                .append(" limit 1")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())

        try {
            val articleSearchResultUploaderLogs =
                    session.createNativeQuery(sqlBuilder.toString(), ArticleSearchResultUploaderLog::class.java).resultList as List<ArticleSearchResultUploaderLog>

            if (articleSearchResultUploaderLogs.size == 1) {
                return articleSearchResultUploaderLogs.get(0)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return null
    }

    fun getUnProcessedArticlesInNewFormatForFirestore(session: Session, uploadDestinationInfo: UploadDestinationInfo, limit:Int = 400): List<Article> {
        val sqlStringBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                                                .append(" WHERE ")
                                                .append(" ${uploadDestinationInfo.uploadFlagName} = 1")
                                                .append(" AND ${uploadDestinationInfo.deleteFlagName} = 0")
                                                .append(" AND processedInNewFormatForFirestore = 0")
                                                .append(" ORDER BY publicationTime asc")
                                                .append(" limit ${limit}")

//        LoggerUtils.logOnConsole(sqlStringBuilder.toString())
        val query = session.createNativeQuery(sqlStringBuilder.toString(), Article::class.java)
        try {
            return query.resultList as List<Article>
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return emptyList()
    }

    fun getLastFirebaseUserInfoSynchronizerLog(session: Session):FirebaseUserInfoSynchronizerLog? {
        val hql = "FROM ${EntityClassNames.FIREBASE_USER_INFO_SYNCHRONIZER_LOG}"

        val query =session.createQuery(hql, FirebaseUserInfoSynchronizerLog::class.java)
        query.list().apply {
            if (isNotEmpty()){
                return this.sortedBy { it.created }.last()
            }
        }
        return null
    }

    fun getAllFirebaseUser(session: Session): List<FirebaseUser> {
        val hql = "FROM ${EntityClassNames.FIREBASE_USER}"
        return session.createQuery(hql, FirebaseUser::class.java).list()
    }

    private fun getFavPageEntriesOnUserSettingsForUser(session: Session,firebaseUser: FirebaseUser):List<FavPageEntryOnUserSettings>{
        val sqlStringBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.FAV_PAGE_ENTRY_ON_USER_SETTINGS_TABLE_NAME}")
                                                    .append(" WHERE ")
                                                    .append(" firebaseUserId = '${firebaseUser.uid}'")

        val query = session.createNativeQuery(sqlStringBuilder.toString(), FavPageEntryOnUserSettings::class.java)
        try {
            return query.resultList as List<FavPageEntryOnUserSettings>
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return emptyList()
    }

    fun deleteFavPageEntriesOnUserSettingsForUser(session: Session,firebaseUserFromDb: FirebaseUser) {
        val currentFavPageEntriesOnUserSettings =
                getFavPageEntriesOnUserSettingsForUser(session = session,firebaseUser = firebaseUserFromDb)
        if (currentFavPageEntriesOnUserSettings.isNotEmpty()){
            runDbTransection(session){
                currentFavPageEntriesOnUserSettings.forEach { session.delete(it) }
            }
        }
    }

    fun findFirebaseUserById(session: Session,userId:String): FirebaseUser? {
        val sqlStringBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.FIREBASE_USER_ENTRY_NAME}")
                                            .append(" WHERE uid='${userId}'")

        val query = session.createNativeQuery(sqlStringBuilder.toString(), FirebaseUser::class.java)
        try {
            (query.resultList as List<FirebaseUser>).apply {
                if (isNotEmpty()){
                    return get(0)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun getFirebaseUserCount(session: Session): Int {

        val sqlBuilder = StringBuilder("SELECT COUNT(*) FROM ${DatabaseTableNames.FIREBASE_USER_ENTRY_NAME}")

        try {
            val result = session.createNativeQuery(sqlBuilder.toString()).list() as List<Int>
            if (result.size == 1) {
                return result.get(0)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return 0
    }

    fun getLatestArticleNotificationGenerationLogForPage(session: Session, parentPage: Page): ArticleNotificationGenerationLog? {

        val sqlStringBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_NOTIFICATION_GENERATION_LOG_TABLE_NAME}")
                                                    .append(" WHERE parentPageId='${parentPage.id}'")
                                                    .append(" ORDER BY created desc limit 1")
//        LoggerUtils.logOnConsole(sqlStringBuilder.toString())
        val query = session.createNativeQuery(sqlStringBuilder.toString(), ArticleNotificationGenerationLog::class.java)
        try {
            (query.resultList as List<ArticleNotificationGenerationLog>).apply {
                if (isNotEmpty()){
                    return get(0)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    fun checkSubscribTionForPage(session: Session, parentPage: Page): Boolean {

        if (!parentPage.topLevelPage!!){return false}

        val sqlBuilder = StringBuilder("SELECT COUNT(*) FROM ${DatabaseTableNames.FAV_PAGE_ENTRY_ON_USER_SETTINGS_TABLE_NAME}")
                                            .append(" WHERE pageId='${parentPage.id}' AND subscribed=true")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())
        try {
            val result = session.createNativeQuery(sqlBuilder.toString()).list() as List<Int>
            if (result.size == 1) {
                return result.get(0) > 0
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return false
    }
}