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
import com.dasbikash.news_server_data_coordinator.exceptions.ArticleDeleteException
import com.dasbikash.news_server_data_coordinator.exceptions.ArticleUploadException
import com.dasbikash.news_server_data_coordinator.exceptions.SettingsUploadException
import com.dasbikash.news_server_data_coordinator.exceptions.handlers.DataCoordinatorExceptionHandler
import com.dasbikash.news_server_data_coordinator.firebase.RealTimeDbDataCoordinatorSettingsUtils
import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import org.hibernate.Session
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random


abstract class DataUploader : Thread() {

    companion object {

        private const val MAX_ARTICLE_INVALID_AGE_ERROR_MESSAGE = "Max article age must be positive"
        private const val MAX_ARTICLE_COUNT_INVALID_ERROR_MESSAGE = "Max article count for upload must be positive"

        private const val WAITING_TIME_FOR_NEW_ARTICLES_FOR_UPLOAD_MS = 10 * 60 * 1000L // 10 mins
        private const val WAITING_TIME_BETWEEN_ITERATION = 5 * 1000L //5 secs

        private const val SQL_DATE_FORMAT = "yyyy-MM-dd"
        private val sqlDateFormatter = SimpleDateFormat(SQL_DATE_FORMAT)

        private const val INIT_DELAY_FOR_ERROR = 5 * 60 * 1000L //5 mins
        private const val MAX_DELAY_FOR_ERROR = 60 * 60 * 1000L //60 mins
        private const val ONE_DAY_IN_MS = 24 * 60 * 60 * 1000L //1 day

        const val MAX_ARTICLE_DELETION_ROUTINE_RUNNING_HOUR = 23
        const val MIN_ARTICLE_DELETION_ROUTINE_RUNNING_HOUR = 0

        private const val MINIMUM_ARTICLE_COUNT_FOR_PAGE = 5
    }

    private var errorDelayPeriod = 0L
    private var errorIteration = 0L

    abstract protected fun getUploadDestinationInfo(): UploadDestinationInfo
    abstract protected fun uploadArticles(articlesForUpload: List<Article>,session: Session): Boolean
    abstract protected fun nukeOldSettings()
    abstract protected fun uploadNewSettings(languages: Collection<Language>, countries: Collection<Country>,
                                             newspapers: Collection<Newspaper>, pages: Collection<Page>,
                                             newsCategories: Collection<NewsCategory>)

    abstract protected fun addToServerUploadTimeLog()
    abstract protected fun deleteArticleFromServer(article: Article,session: Session): Boolean


    private fun getMaxArticleAgeInDays(): Int =
            RealTimeDbDataCoordinatorSettingsUtils
                    .getArticleUploadSettingsForTarget(getUploadDestinationInfo().articleUploadTarget).MAX_ARTICLE_AGE_DAYS //Must be >=0

    private fun maxArticleCountForUpload(): Int =
            RealTimeDbDataCoordinatorSettingsUtils
                    .getArticleUploadSettingsForTarget(getUploadDestinationInfo().articleUploadTarget).MAX_ARTICLE_COUNT_FOR_UPLOAD // Must be >=0

    private fun getMaxArticleCountForPage(): Int =
            RealTimeDbDataCoordinatorSettingsUtils
                    .getArticleDeletionSettingsForTarget(getUploadDestinationInfo().articleUploadTarget).MAX_ARTICLE_COUNT_FOR_PAGE

    private fun getDailyArticleDeletionLimit(): Int =
            RealTimeDbDataCoordinatorSettingsUtils
                    .getArticleDeletionSettingsForTarget(getUploadDestinationInfo().articleUploadTarget).DAILY_ARTICLE_DELETION_LIMIT

    private fun getMaxArticleDeletionChunkSize(): Int =
            RealTimeDbDataCoordinatorSettingsUtils
                    .getArticleDeletionSettingsForTarget(getUploadDestinationInfo().articleUploadTarget).MAX_ARTICLE_DELETION_CHUNK_SIZE

    private fun getArticleDeletionRoutineRunningHour(): Int =
            RealTimeDbDataCoordinatorSettingsUtils
                    .getArticleDeletionSettingsForTarget(getUploadDestinationInfo().articleUploadTarget).ARTICLE_DELETION_ROUTINE_RUNNING_HOUR

    private fun serveArticleDeleteRequest(session: Session, articleDeleteRequest: ArticleDeleteRequest) {
        getArticlesForDeletion(session, articleDeleteRequest.page!!, articleDeleteRequest.deleteRequestCount!!).asSequence().forEach {
            if (deleteArticleFromServer(it,session)) {
                DatabaseUtils.markArticleAsDeletedFromDataStore(session, it, getUploadDestinationInfo())
            }
        }
    }


    private fun getArticlesForDeletion(session: Session, page: Page, deleteRequestCount: Int): List<Article> {
        return DatabaseUtils.getArticlesForDeletion(
                session, page, deleteRequestCount, getUploadDestinationInfo())
    }


    private fun getPendingArticleDeleteRequest(session: Session): ArticleDeleteRequest? {
        val deleteRequests = DatabaseUtils.getArticleDeleteRequests(session, getUploadDestinationInfo().articleUploadTarget)
        if (deleteRequests.isNotEmpty()) {
            return deleteRequests.get(0)
        }
        return null
    }

    private fun logArticleDeleteRequestServing(session: Session, articleDeleteRequest: ArticleDeleteRequest) {
        articleDeleteRequest.served = true
        DatabaseUtils.runDbTransection(session) { session.update(articleDeleteRequest) }
    }

    private fun getErrorDelayPeriod(): Long {
//        errorIteration++
//        errorDelayPeriod += (INIT_DELAY_FOR_ERROR * errorIteration)
        errorDelayPeriod += INIT_DELAY_FOR_ERROR

        if (errorDelayPeriod > MAX_DELAY_FOR_ERROR) {
            errorDelayPeriod = MAX_DELAY_FOR_ERROR
        }

        return errorDelayPeriod
    }

    private fun getSqlForArticleFetch(): String {
        if (maxArticleCountForUpload() < 0) {
            throw IllegalArgumentException(MAX_ARTICLE_COUNT_INVALID_ERROR_MESSAGE)
        }
        val sqlBuilder = StringBuilder("SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE")
                .append(" ${Article.PUBLICATION_TIME_COLUMN_NAME} > '${getMinArticleDateString()}'")
                .append(" AND ${getUploadDestinationInfo().uploadFlagName}=0")
                .append(" ORDER BY ${Article.PUBLICATION_TIME_COLUMN_NAME} DESC")
                .append(" LIMIT ${maxArticleCountForUpload()}")
        return sqlBuilder.toString()
    }

    private fun getMinArticleDateString(): String {
        if (getMaxArticleAgeInDays() < 0) {
            throw IllegalArgumentException(MAX_ARTICLE_INVALID_AGE_ERROR_MESSAGE)
        }
        val today = Calendar.getInstance()
        today.add(Calendar.DAY_OF_YEAR, -1 * getMaxArticleAgeInDays())
        return sqlDateFormatter.format(today.time)
    }

    private fun getUploadedArticleCountForPage(session: Session, page: Page): Int {
        val sqlBuilder = StringBuilder("SELECT COUNT(*) FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                .append(" WHERE")
                .append(" pageId='${page.id}'")
                .append(" AND ${getUploadDestinationInfo().uploadFlagName}=1")
                .append(" AND ${getUploadDestinationInfo().deleteFlagName}=0")

//        LoggerUtils.logOnConsole(sqlBuilder.toString())

        return (session.createNativeQuery(sqlBuilder.toString()).list() as List<Int>).get(0)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getArticlesForUpload(session: Session): List<Article> {
        if (getUploadDestinationInfo().articleUploadTarget == ArticleUploadTarget.FIRE_STORE_DB){
            val unProcessedArticlesInNewFormatForFirestore =
                    DatabaseUtils.getUnProcessedArticlesInNewFormatForFirestore(session,getUploadDestinationInfo())
            if (unProcessedArticlesInNewFormatForFirestore.isNotEmpty()){
                return unProcessedArticlesInNewFormatForFirestore
            }
        }
        val sql = getSqlForArticleFetch()
        return session.createNativeQuery(sql, Article::class.java).resultList as List<Article>
    }

    fun getSqlToMarkUploadedArticle(article: Article): String {
        val sqlBuilder = StringBuilder("UPDATE ${DatabaseTableNames.ARTICLE_TABLE_NAME}")
                                        .append(" SET ${getUploadDestinationInfo().uploadFlagName}=1")

        if (getUploadDestinationInfo().articleUploadTarget == ArticleUploadTarget.FIRE_STORE_DB){
            sqlBuilder.append(",processedInNewFormatForFirestore=1")
        }

        sqlBuilder.append(" WHERE id='${article.id}'")
//        return "UPDATE ${DatabaseTableNames.ARTICLE_TABLE_NAME} SET ${getUploadDestinationInfo().uploadFlagName}=1 WHERE id='${article.id}'"
        return sqlBuilder.toString()
    }

    private fun markArticlesAsUploaded(articlesForUpload: List<Article>, session: Session) {
        articlesForUpload.asSequence()
                .forEach {
                    val sql = getSqlToMarkUploadedArticle(it)
                    DatabaseUtils.runDbTransection(session) {
                        session.createNativeQuery(sql).executeUpdate()
                    }
                }
    }

    private fun checkIfSettingsModified(session: Session): Boolean {
        val lastSettingsUpdateLog = DatabaseUtils.getLastSettingsUpdateLog(session)
        val lastSettingsUploadLog = DatabaseUtils
                .getLastSettingsUploadLogByTarget(session, getUploadDestinationInfo().articleUploadTarget)

        if (lastSettingsUploadLog != null) {
            if (lastSettingsUploadLog.uploadTime > lastSettingsUpdateLog.updateTime) {
                return false
            }
        }
        return true
    }

    private fun addSettingsUpdateLog(session: Session) {
        DatabaseUtils.runDbTransection(session) {
            session.save(SettingsUploadLog(uploadTarget = getUploadDestinationInfo().articleUploadTarget))
        }
    }

    private fun uploadSettingsToServer(session: Session) {
        val languages = DatabaseUtils.getLanguageMap(session).values.filter { it.updated }
        val countries = DatabaseUtils.getCountriesMap(session).values.filter { it.updated }
        val newspapers = DatabaseUtils.getNewspaperMap(session).values.filter { it.updated }
        val pages = DatabaseUtils.getPageMapForAll(session).values.filter { it.updated }
        val newsCategories = DatabaseUtils.getNewsCategoryMap(session).values.filter { it.updated }

        if (languages.isEmpty() && countries.isEmpty() && newspapers.isEmpty()
                && pages.isEmpty() && newsCategories.isEmpty()) {
            addSettingsUpdateLog(session)
            throw IllegalArgumentException("No settings to update!!!")
        }
//        nukeOldSettings()
        uploadNewSettings(languages, countries, newspapers, pages, newsCategories)
        addToServerUploadTimeLog()
        addSettingsUpdateLog(session)
        resetUpdatedFlags(session,languages, countries, newspapers, pages, newsCategories)
    }

    private fun resetUpdatedFlags(session: Session, languages: List<Language>, countries: List<Country>,
                                  newspapers: List<Newspaper>, pages: List<Page>, newsCategories: List<NewsCategory>) {
        val taskList = mutableListOf<() -> Unit>()
        languages.asSequence().forEach {
            it.updated = false
            taskList.add({session.update(it)})
        }
        countries.asSequence().forEach {
            it.updated = false
            taskList.add({session.update(it)})
        }
        newspapers.asSequence().forEach {
            it.updated = false
            taskList.add({session.update(it)})
        }
        pages.asSequence().forEach {
            it.updated = false
            taskList.add({session.update(it)})
        }
        newsCategories.asSequence().forEach {
            it.updated = false
            taskList.add({session.update(it)})
        }
        DatabaseUtils.runDbTransections(session,taskList)
    }

    override fun run() {
        RealTimeDbDataCoordinatorSettingsUtils.init()
        sleep(Random(System.currentTimeMillis()).nextLong(5000L) + 60000L)
        do {
            val session = DbSessionManager.getNewSession()

            if (!DatabaseUtils.getArticleUploaderStatus(session, getUploadDestinationInfo().articleUploadTarget)) {
                logExit(session)
                session.close()
                return
            }

            try {
                if (checkIfSettingsModified(session)) {
                    uploadSettingsToServer(session)
                    resetErrorDelay()
                }
            } catch (ex: Throwable) {
                session.close()
                ex.printStackTrace()
                DataCoordinatorExceptionHandler.handleException(
                        SettingsUploadException(getUploadDestinationInfo().articleUploadTarget, ex)
                )
                waitHere(getErrorDelayPeriod())
                continue
            }

            try {
                val pendingArticleDeleteRequest = getPendingArticleDeleteRequest(session)
                if (pendingArticleDeleteRequest != null) {
                    LoggerUtils.logOnConsole("target: ${getUploadDestinationInfo().articleUploadTarget.name} request: ${pendingArticleDeleteRequest}")
                    serveArticleDeleteRequest(session, pendingArticleDeleteRequest)
                    logArticleDeleteRequestServing(session, pendingArticleDeleteRequest)
                } else if (needToRunDailyArticleDeletionTask(session)) {
                    runDailyArticleDeletionTask(session)
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
                DataCoordinatorExceptionHandler.handleException(
                        ArticleDeleteException(getUploadDestinationInfo().articleUploadTarget, ex)
                )
            }

            val articlesForUpload = getArticlesForUpload(session)
            LoggerUtils.logOnConsole("articlesForUpload.size: ${articlesForUpload.size} for ${getUploadDestinationInfo().articleUploadTarget.name}")
            if (articlesForUpload.size > 0) {
                try {
                    if (uploadArticles(articlesForUpload,session)) {
                        markArticlesAsUploaded(articlesForUpload, session)
                        LoggerUtils.logArticleUploadHistory(session, articlesForUpload, getUploadDestinationInfo())
                        resetErrorDelay()
                    }
                } catch (ex: Throwable) {
                    session.close()
                    ex.printStackTrace()
                    DataCoordinatorExceptionHandler.handleException(
                            ArticleUploadException(getUploadDestinationInfo().articleUploadTarget, ex))
                    waitHere(getErrorDelayPeriod())
                    continue
                }

                session.close()
                waitHere(WAITING_TIME_BETWEEN_ITERATION)

            } else {
                session.close()
                waitHere(WAITING_TIME_FOR_NEW_ARTICLES_FOR_UPLOAD_MS +
                        Random(System.currentTimeMillis()).nextLong(WAITING_TIME_FOR_NEW_ARTICLES_FOR_UPLOAD_MS))
            }
        } while (true)
    }

    private fun runDailyArticleDeletionTask(session: Session) {
        LoggerUtils.logOnDb("Starting daily article deletion task for ${getUploadDestinationInfo().articleUploadTarget.name}", session)
        val deletedArticleIds = mutableListOf<String>()
        DatabaseUtils.getAllPages(session)./*shuffled().*/asSequence()
                .filter { (it.hasData ?: false) && deletedArticleIds.size < getDailyArticleDeletionLimit() }.forEach {
                    //                    println(it)
                    do {
                        val currrentArticleCountInTargetForPage =
                                DatabaseUtils.getArticleCountInTargetForPage(session, it, getUploadDestinationInfo())
//                        println("currrentArticleCountInTargetForPage: ${currrentArticleCountInTargetForPage}")
                        if (currrentArticleCountInTargetForPage <= getMaxArticleCountForPage()) {
                            break//return@forEach
                        }

                        val articleDeletionChunkSize = when {
                            (currrentArticleCountInTargetForPage - getMaxArticleCountForPage()) > getMaxArticleDeletionChunkSize()
                            -> getMaxArticleDeletionChunkSize()
                            else -> currrentArticleCountInTargetForPage - getMaxArticleCountForPage()
                        }
//                        println("articleDeletionChunkSize: ${articleDeletionChunkSize}")
                        var articleDeletionCountForPage = 0
                        getArticlesForDeletion(session, it, articleDeletionChunkSize).asSequence().forEach {
                            //                            println(it)
                            if (deleteArticleFromServer(it,session)) {
                                DatabaseUtils.markArticleAsDeletedFromDataStore(session, it, getUploadDestinationInfo())
                                deletedArticleIds.add(it.id)
                                articleDeletionCountForPage++
                            }
                        }
                        LoggerUtils.logOnDb("${articleDeletionCountForPage} articles deleted of page ${it.name} | ${it.id} from ${getUploadDestinationInfo().articleUploadTarget.name}", session)

                    } while (deletedArticleIds.size < getDailyArticleDeletionLimit())
//                        println("deletedArticleIds.size: ${deletedArticleIds.size}")
                }
        DatabaseUtils.runDbTransection(session) {
            session.save(DailyDeletionTaskLog.getInstance(getUploadDestinationInfo().articleUploadTarget, deletedArticleIds))
        }
        LoggerUtils.logOnDb("${deletedArticleIds.size} articles deleted from ${getUploadDestinationInfo().articleUploadTarget.name}", session)
    }

    private fun needToRunDailyArticleDeletionTask(session: Session): Boolean {
        if (getArticleDeletionRoutineRunningHour() > MAX_ARTICLE_DELETION_ROUTINE_RUNNING_HOUR ||
                getArticleDeletionRoutineRunningHour() < MIN_ARTICLE_DELETION_ROUTINE_RUNNING_HOUR) {
            return false
        }
        val now = Calendar.getInstance()
        if (now.get(Calendar.HOUR_OF_DAY) < getArticleDeletionRoutineRunningHour()) {
            return false
        }
        val lastDeletionTaskLog =
                DatabaseUtils.getLastDeletionTaskLogForTarget(session, getUploadDestinationInfo().articleUploadTarget)

        return (lastDeletionTaskLog == null) ||
                (now.timeInMillis - lastDeletionTaskLog.created!!.time) > ONE_DAY_IN_MS
    }

    private fun waitHere(waitTimeMs: Long) {
        try {
            sleep(waitTimeMs)
        } catch (ex: InterruptedException) {
            ex.printStackTrace()
        }
    }

    private fun logExit(session: Session? = null) {
        LoggerUtils.logOnConsole("Exiting ${getUploadDestinationInfo().articleUploadTarget.name} article uploader.")
        session?.let {
            LoggerUtils.logOnDb("Exiting ${getUploadDestinationInfo().articleUploadTarget.name} " +
                    "article uploader.", session)
        }
    }

    private fun resetErrorDelay() {
        errorDelayPeriod = 0L
        errorIteration = 0L
    }
}