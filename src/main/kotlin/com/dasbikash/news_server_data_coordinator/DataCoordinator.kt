@file:JvmName("com.dasbikash.news_server_data_coordinator.DataCoordinator")

package com.dasbikash.news_server_data_coordinator

import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploader
import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploaderForFireStoreDb
import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploaderForMongoRestService
import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploaderForRealTimeDb
import com.dasbikash.news_server_data_coordinator.article_fetcher.ArticleFetcher
import com.dasbikash.news_server_data_coordinator.article_search_result_processor.ArticleSearchReasultProcessor
import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.exceptions.AppInitException
import com.dasbikash.news_server_data_coordinator.exceptions.DataCoordinatorException
import com.dasbikash.news_server_data_coordinator.exceptions.ReportGenerationException
import com.dasbikash.news_server_data_coordinator.exceptions.handlers.DataCoordinatorExceptionHandler
import com.dasbikash.news_server_data_coordinator.firebase.RealTimeDbAdminTaskUtils
import com.dasbikash.news_server_data_coordinator.firebase.RealTimeDbFcmUtils
import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget
import com.dasbikash.news_server_data_coordinator.model.db_entity.SettingsUpdateLog
import com.dasbikash.news_server_data_coordinator.settings_loader.DataFetcherFromParser
import com.dasbikash.news_server_data_coordinator.utils.DateUtils
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import com.dasbikash.news_server_data_coordinator.utils.ReportGenerationUtils
import org.hibernate.Session
import java.util.*

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


object DataCoordinator {

    private var mArticleFetcher: ArticleFetcher? = null
    private val DATA_COORDINATOR_ITERATION_PERIOD = 15 * 60 * 1000L //15 mins
    private lateinit var realTimeDbDataUploader: DataUploader
    private lateinit var fireStoreDbDataUploader: DataUploader
    private lateinit var mongoRestDataUploader: DataUploader
    private val INIT_DELAY_FOR_ERROR = 60 * 1000L
    private var errorDelayPeriod = 0L
    private var errorIteration = 0L

    private lateinit var currentDate: Calendar
    private var articleSearchReasultProcessor: ArticleSearchReasultProcessor? = null

    @JvmStatic
    fun main(args: Array<String>) {
        currentDate = Calendar.getInstance()
        do {
            try {

                updateSettingsIfChanged()
//
                refreshArticleFetcher()
                refreshArticleDataUploaders()
                refreshArticleSearchReasultProcessor()

                errorDelayPeriod = 0L
                errorIteration = 0L

                val now = Calendar.getInstance()
                if (now.get(Calendar.YEAR) > currentDate.get(Calendar.YEAR) ||
                        now.get(Calendar.DAY_OF_YEAR) > currentDate.get(Calendar.DAY_OF_YEAR)) {
                    try {
                        val session = DbSessionManager.getNewSession()

                        generateAndDistributeDailyReport(now.time!!, session)

                        if (DateUtils.isFirstDayOfWeek(now.time)) {
                            generateAndDistributeWeeklyReport(now.time, session)
                        }

                        if (DateUtils.isFirstDayOfMonth(now.time)) {
                            generateAndDistributeMonthlyReport(now.time, session)
                        }
                        session.close()

                        currentDate = now
                    } catch (ex: Throwable) {
                        ex.printStackTrace()
                        DataCoordinatorExceptionHandler.handleException(ReportGenerationException(ex))
                    }
                }
                RealTimeDbAdminTaskUtils.init()
                RealTimeDbFcmUtils.init()
                Thread.sleep(DATA_COORDINATOR_ITERATION_PERIOD)
            } catch (ex: DataCoordinatorException) {
                DataCoordinatorExceptionHandler.handleException(ex)
                ex.printStackTrace()
                try {
                    Thread.sleep(getErrorDelayPeriod())
                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                }

            } catch (ex: Exception) {
                ex.printStackTrace()
                val session = DbSessionManager.getNewSession()
                try {
                    LoggerUtils.logError(ex, session)
                    Thread.sleep(getErrorDelayPeriod())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    if (session.isOpen) {
                        session.close()
                    }
                }
            }

        } while (true)
    }

    private fun generateAndDistributeDailyReport(today: Date, session: Session) {
        LoggerUtils.logOnConsole("Starting daily data-coordinator activity report generation.")
        ReportGenerationUtils.prepareDailyReport(today, session)
        LoggerUtils.logOnConsole("Daily data-coordinator activity report generated.")
        ReportGenerationUtils.emailDailyReport(today)
        LoggerUtils.logOnConsole("Daily data-coordinator activity report distributed.")
    }

    private fun generateAndDistributeWeeklyReport(today: Date, session: Session) {
        LoggerUtils.logOnConsole("Starting weekly data-coordinator activity report generation.")
        ReportGenerationUtils.prepareWeeklyReport(today, session)
        LoggerUtils.logOnConsole("Weekly data-coordinator activity report generated.")
        ReportGenerationUtils.emailWeeklyReport(today)
        LoggerUtils.logOnConsole("Weekly data-coordinator activity report distributed.")
    }

    private fun generateAndDistributeMonthlyReport(today: Date, session: Session) {
        LoggerUtils.logOnConsole("Starting monthly data-coordinator activity report generation.")
        ReportGenerationUtils.prepareMonthlyReport(today, session)
        LoggerUtils.logOnConsole("Monthly data-coordinator activity report generated.")
        ReportGenerationUtils.emailMonthlyReport(today)
        LoggerUtils.logOnConsole("Monthly data-coordinator activity report distributed.")
    }

    private fun getErrorDelayPeriod(): Long {
        errorIteration++
        errorDelayPeriod += (INIT_DELAY_FOR_ERROR * errorIteration)
        return errorDelayPeriod
    }

    private fun refreshArticleFetcher() {
        try {
            val session = DbSessionManager.getNewSession()

            val activeNewspapers = DatabaseUtils.getNewspaperMap(session).values.filter { it.active }.toList()
            if (mArticleFetcher == null) {
                if (activeNewspapers.isNotEmpty()) {
                    mArticleFetcher = ArticleFetcher()
                    mArticleFetcher!!.start()
                }
            } else {
                if (activeNewspapers.isEmpty()) {
                    mArticleFetcher!!.interrupt()
                    mArticleFetcher = null
                } else if (!mArticleFetcher!!.isAlive) {
                    mArticleFetcher = ArticleFetcher()
                    mArticleFetcher!!.start()
                }
            }
            session.close()
        } catch (ex: Exception) {
            throw AppInitException(ex)
        }
    }

    private fun refreshArticleDataUploaders() {
        try {
            val session = DbSessionManager.getNewSession()
            if (isRealTimeDbDataUploadEnabled(session)) {
                if (!::realTimeDbDataUploader.isInitialized) {
                    realTimeDbDataUploader = DataUploaderForRealTimeDb()
                    realTimeDbDataUploader.start()
                } else {
                    if (!realTimeDbDataUploader.isAlive) {
                        realTimeDbDataUploader = DataUploaderForRealTimeDb()
                        realTimeDbDataUploader.start()
                    }
                }
            } else {
                if (::realTimeDbDataUploader.isInitialized && realTimeDbDataUploader.isAlive) {
                    realTimeDbDataUploader.interrupt()
                }
            }

            if (isFireStoreDbDataUploadEnabled(session)) {
                if (!::fireStoreDbDataUploader.isInitialized) {
                    fireStoreDbDataUploader = DataUploaderForFireStoreDb()
                    fireStoreDbDataUploader.start()
                } else {
                    if (!fireStoreDbDataUploader.isAlive) {
                        fireStoreDbDataUploader = DataUploaderForFireStoreDb()
                        fireStoreDbDataUploader.start()
                    }
                }
            } else {
                if (::fireStoreDbDataUploader.isInitialized && fireStoreDbDataUploader.isAlive) {
                    fireStoreDbDataUploader.interrupt()
                }
            }

            if (isMongoRestDataUploadEnabled(session)) {
                if (!::mongoRestDataUploader.isInitialized) {
                    mongoRestDataUploader = DataUploaderForMongoRestService()
                    mongoRestDataUploader.start()
                } else {
                    if (!mongoRestDataUploader.isAlive) {
                        mongoRestDataUploader = DataUploaderForMongoRestService()
                        mongoRestDataUploader.start()
                    }
                }
            } else {
                if (::mongoRestDataUploader.isInitialized && mongoRestDataUploader.isAlive) {
                    mongoRestDataUploader.interrupt()
                }
            }
            session.close()
        } catch (ex: Exception) {
            throw AppInitException(ex)
        }
    }

    private fun refreshArticleSearchReasultProcessor() {
        if (articleSearchReasultProcessor != null) {
            if (!articleSearchReasultProcessor!!.isAlive) {
                articleSearchReasultProcessor = null
            }
        }

        if (articleSearchReasultProcessor == null) {
            articleSearchReasultProcessor = ArticleSearchReasultProcessor.getInstance()
            articleSearchReasultProcessor?.start()
        }
    }

    private fun updateSettingsIfChanged() {
        try {

            val session = DbSessionManager.getNewSession()

            var settingsUpdated = false
            val settingsUpdateLogMessageBuilder = StringBuilder()

            fun setSettingsUpdated() {
                settingsUpdated = true
            }

            Pair(DataFetcherFromParser.getLanguageMap(), DatabaseUtils.getLanguageMap(session))
                    .apply {
                        first.keys.asSequence().forEach {
                            if (second.contains(it)) {
                                if (!first.get(it)!!.equals(second.get(it))) {
                                    setSettingsUpdated()
                                    settingsUpdateLogMessageBuilder.append("Language modified id: ${it} | ")
                                    val oldLanguage = second.get(it)
                                    val newLanguage = first.get(it)
                                    oldLanguage!!.updateData(newLanguage!!)
                                    DatabaseUtils.runDbTransection(session) {
                                        session.update(oldLanguage)
                                    }
                                }
                            } else {
                                setSettingsUpdated()
                                settingsUpdateLogMessageBuilder.append("Language added id: ${it} | ")
                                DatabaseUtils.runDbTransection(session) {
                                    session.save(first.get(it))
                                }
                            }
                        }
                    }

            Pair(DataFetcherFromParser.getCountryMap(), DatabaseUtils.getCountriesMap(session))
                    .apply {
                        first.keys.asSequence().forEach {
                            if (second.contains(it)) {
                                if (!first.get(it)!!.equals(second.get(it))) {
                                    setSettingsUpdated()
                                    settingsUpdateLogMessageBuilder.append("Country modified id: ${it} | ")
                                    val oldCountry = second.get(it)
                                    val newCountry = first.get(it)
                                    oldCountry!!.updateData(newCountry!!)
                                    DatabaseUtils.runDbTransection(session) {
                                        session.update(oldCountry)
                                    }
                                }
                            } else {
                                setSettingsUpdated()
                                settingsUpdateLogMessageBuilder.append("Country added id: ${it} | ")
                                DatabaseUtils.runDbTransection(session) {
                                    session.save(first.get(it))
                                }
                            }
                        }

                    }

            Pair(DataFetcherFromParser.getNewspaperMap(session), DatabaseUtils.getNewspaperMap(session))
                    .apply {
                        first.keys.asSequence().forEach {
                            if (second.contains(it)) {
                                if (!first.get(it)!!.equals(second.get(it))) {
                                    setSettingsUpdated()
                                    settingsUpdateLogMessageBuilder.append("Newspaper modified id: ${it} | ")
                                    val oldNewspaper = second.get(it)
                                    val newNewspaper = first.get(it)
                                    oldNewspaper!!.updateData(newNewspaper!!)
                                    DatabaseUtils.runDbTransection(session) {
                                        session.update(oldNewspaper)
                                    }
                                }
                            } else {
                                setSettingsUpdated()
                                settingsUpdateLogMessageBuilder.append("Newspaper added id: ${it} | ")
                                DatabaseUtils.runDbTransection(session) {
                                    session.save(first.get(it))
                                }
                            }
                        }
                    }

            Pair(DataFetcherFromParser.getPageMap(session), DatabaseUtils.getPageMapForAll(session))
                    .apply {
                        first.keys.asSequence().forEach {
                            if (second.contains(it)) {
                                if (!first.get(it)!!.equals(second.get(it))) {
                                    setSettingsUpdated()
                                    settingsUpdateLogMessageBuilder.append("Page modified id: ${it} | ")
                                    val oldPage = second.get(it)
                                    val newPage = first.get(it)
                                    oldPage!!.updateData(newPage!!)
                                    DatabaseUtils.runDbTransection(session) {
                                        session.update(oldPage)
                                    }
                                }
                            } else {
                                setSettingsUpdated()
                                settingsUpdateLogMessageBuilder.append("Page added id: ${it} | ")
                                DatabaseUtils.runDbTransection(session) {
                                    session.save(first.get(it))
                                }
                            }
                        }
                    }

            Pair(DataFetcherFromParser.getNewsCategoryMap(), DatabaseUtils.getNewsCategoryMap(session))
                    .apply {
                        first.keys.asSequence().forEach {
                            if (second.contains(it)) {
                                if (!first.get(it)!!.equals(second.get(it))) {
                                    setSettingsUpdated()
                                    settingsUpdateLogMessageBuilder.append("NewsCategory modified id: ${it} | ")
                                    val oldNewsCategory = second.get(it)
                                    val newNewsCategory = first.get(it)
                                    oldNewsCategory!!.updateData(newNewsCategory!!)
                                    DatabaseUtils.runDbTransection(session) {
                                        session.update(oldNewsCategory)
                                    }
                                }
                            } else {
                                setSettingsUpdated()
                                settingsUpdateLogMessageBuilder.append("NewsCategory added id: ${it} | ")
                                DatabaseUtils.runDbTransection(session) {
                                    session.save(first.get(it))
                                }
                            }
                        }
                    }

            Pair(DataFetcherFromParser.getNewsCategoryEntryMap(session), DatabaseUtils.getNewsCategoryEntryMap(session))
                    .apply {
                        first.keys.asSequence().forEach {
                            if (!second.contains(it)) {
                                DatabaseUtils.runDbTransection(session) {
                                    session.save(first.get(it))
                                }
                            }
                        }
                    }

            if (settingsUpdated) {
                val logMessage = settingsUpdateLogMessageBuilder.toString()
                                            .substringBeforeLast('|')//trailing " | " striped
                DatabaseUtils.runDbTransection(session) {
                    session.save(SettingsUpdateLog(logMessage = logMessage))
                }
                LoggerUtils.logOnConsole(logMessage)
            }
            session.close()
        } catch (ex: Exception) {
            throw AppInitException(ex)
        }
    }

    private fun isRealTimeDbDataUploadEnabled(session: Session): Boolean {
        return DatabaseUtils.getArticleUploaderStatus(session, ArticleUploadTarget.REAL_TIME_DB)
    }

    private fun isFireStoreDbDataUploadEnabled(session: Session): Boolean {
        return DatabaseUtils.getArticleUploaderStatus(session, ArticleUploadTarget.FIRE_STORE_DB)
    }

    private fun isMongoRestDataUploadEnabled(session: Session): Boolean {
        return DatabaseUtils.getArticleUploaderStatus(session, ArticleUploadTarget.MONGO_REST_SERVICE)
    }
}