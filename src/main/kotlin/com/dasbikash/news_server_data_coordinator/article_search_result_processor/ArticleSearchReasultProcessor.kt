package com.dasbikash.news_server_data_coordinator.article_search_result_processor

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.exceptions.HighestLevelException
import com.dasbikash.news_server_data_coordinator.exceptions.handlers.DataCoordinatorExceptionHandler
import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleSearchResultUploaderLog
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import org.hibernate.Session

class ArticleSearchReasultProcessor private constructor() : Thread() {
    companion object {

        private const val ARTICLE_SEARCH_RESULT_PROCESSING_ON = true

        private const val ONE_MINUTE_IN_MS = 60 * 1000L
        private const val ONE_HOUR_IN_MS = 60 * 60 * 1000L
        private const val ONE_DAY_IN_MS = 24 * 60 * 60 * 1000L

        private const val INIT_DELAY_MS = 5 * ONE_MINUTE_IN_MS
        private const val DURATION_BETWEEN_UPLOADER_RUN_MS = 1 * ONE_HOUR_IN_MS
        private const val MAX_UPLOADER_RUN_PERIOD_MS = 1 * ONE_HOUR_IN_MS
        private const val MAX_ARTICLE_PROCESSING_ROUTINE_RUN_PERIOD_MS = 1 * ONE_HOUR_IN_MS
        private const val SLEEP_PERIOD_BETWEEN_ITERATION_MS = 30 * ONE_MINUTE_IN_MS

        private const val ARTICLE_SEARCH_RESULT_UPLOAD_CHUNK_SIZE = 400
        private const val ARTICLE_PROCESSING_CHUNK_SIZE = 200

        fun getInstance(): ArticleSearchReasultProcessor? {
            if (ARTICLE_SEARCH_RESULT_PROCESSING_ON) {
                return ArticleSearchReasultProcessor()
            }
            return null
        }
    }

    lateinit var dbSession: Session

    override fun run() {
        try {
            sleep(INIT_DELAY_MS)
            while (true) {

                processUnProcessedArticlesForSearchResults()

                if (checkIfNeedToRunSearchResultUploader()) {
                    uploadNewArticleSearchResults()
                }

                sleep(SLEEP_PERIOD_BETWEEN_ITERATION_MS)
            }
        } catch (ex: Throwable) {
            LoggerUtils.logOnConsole("Article Search Reasult Processor Exit.")
            DataCoordinatorExceptionHandler
                    .handleException(HighestLevelException("Article Search Reasult Processor Exit.",ex))
        }
    }

    private fun uploadNewArticleSearchResults() {
        LoggerUtils.logOnDb("Starting New Article Search Results upload routine.",getDatabaseSession())
        val startTime = System.currentTimeMillis()
        var uploadedSearchResultCount = 0
        var newKeyWordSearchResults = DatabaseUtils.getNewKeyWordSearchResults(getDatabaseSession(),ARTICLE_SEARCH_RESULT_UPLOAD_CHUNK_SIZE)
        do {
            ArticleSearchResultUtils.writeKeyWordSearchResults(newKeyWordSearchResults,getDatabaseSession())
            uploadedSearchResultCount += newKeyWordSearchResults.size
            newKeyWordSearchResults = DatabaseUtils.getNewKeyWordSearchResults(getDatabaseSession(),ARTICLE_SEARCH_RESULT_UPLOAD_CHUNK_SIZE)
        } while (newKeyWordSearchResults.isNotEmpty() && ((System.currentTimeMillis() - startTime) < MAX_UPLOADER_RUN_PERIOD_MS))

        DatabaseUtils.runDbTransection(getDatabaseSession()){
            getDatabaseSession().save(ArticleSearchResultUploaderLog(
                    logMessage = "${uploadedSearchResultCount} new entries uploaded."
            ))
        }
        LoggerUtils.logOnDb("Exit of New Article Search Results upload routine",getDatabaseSession())
    }

    private fun processUnProcessedArticlesForSearchResults() {
        LoggerUtils.logOnDb("Starting routine for processing Articles for Search Results",getDatabaseSession())
        var processedArticleCount = 0
        val startTime = System.currentTimeMillis()
        var unProcessedArticles = DatabaseUtils.getUnProcessedArticlesForSearchResult(getDatabaseSession(),ARTICLE_PROCESSING_CHUNK_SIZE)
        while (unProcessedArticles.isNotEmpty() && ((System.currentTimeMillis() - startTime) < MAX_ARTICLE_PROCESSING_ROUTINE_RUN_PERIOD_MS)) {
            unProcessedArticles.asSequence().forEach {
                ArticleSearchResultUtils.processArticleForSearchResult(getDatabaseSession(), it)
            }
            processedArticleCount += unProcessedArticles.size
            LoggerUtils.logOnDb("${unProcessedArticles.size} articles processed for Search Results in current iteration.",getDatabaseSession())
            unProcessedArticles = DatabaseUtils.getUnProcessedArticlesForSearchResult(getDatabaseSession(),ARTICLE_PROCESSING_CHUNK_SIZE)
        }
        LoggerUtils.logOnDb("Total ${processedArticleCount} articles processed for Search Results",getDatabaseSession())
        LoggerUtils.logOnDb("Exiting routine for processing Articles for Search Results",getDatabaseSession())
    }

    private fun checkIfNeedToRunSearchResultUploader(): Boolean {
        val lastRunLog = DatabaseUtils.getLastArticleSearchResultUploaderLog(getDatabaseSession())
        if (lastRunLog == null) {
            return true
        }
        getDatabaseSession().refresh(lastRunLog)
        return (System.currentTimeMillis() - lastRunLog.created!!.time) > DURATION_BETWEEN_UPLOADER_RUN_MS
    }

    private fun getDatabaseSession(): Session {
        if (!::dbSession.isInitialized || !dbSession.isOpen) {
            dbSession = DbSessionManager.getNewSession()
        }
        return dbSession
    }
}