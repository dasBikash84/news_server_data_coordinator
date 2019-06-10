@file:JvmName("com.dasbikash.news_server_data_coordinator.DataCoordinator")

package com.dasbikash.news_server_data_coordinator

import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploader
import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploaderForFireStoreDb
import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploaderForMongoRestService
import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploaderForRealTimeDb
import com.dasbikash.news_server_data_coordinator.article_fetcher.ArticleFetcher
import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.exceptions.AppInitException
import com.dasbikash.news_server_data_coordinator.exceptions.DataCoordinatorException
import com.dasbikash.news_server_data_coordinator.exceptions.ReportGenerationException
import com.dasbikash.news_server_data_coordinator.exceptions.handlers.DataCoordinatorExceptionHandler
import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget
import com.dasbikash.news_server_data_coordinator.model.db_entity.SettingsUpdateLog
import com.dasbikash.news_server_data_coordinator.settings_loader.DataFetcherFromParser
import com.dasbikash.news_server_data_coordinator.utils.DateUtils
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import com.dasbikash.news_server_data_coordinator.utils.ReportGenerationUtils
import org.hibernate.Session
import java.util.*
import kotlin.collections.ArrayList

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

    private val ARTICLE_FETCHER_MAP: MutableMap<String, ArticleFetcher> = mutableMapOf()
    private val DATA_COORDINATOR_ITERATION_PERIOD = 15 * 60 * 1000L //15 mins
    private lateinit var realTimeDbDataUploader: DataUploader
    private lateinit var fireStoreDbDataUploader: DataUploader
    private lateinit var mongoRestDataUploader: DataUploader
    private val INIT_DELAY_FOR_ERROR = 60 * 1000L
    private var errorDelayPeriod = 0L
    private var errorIteration = 0L

    private lateinit var currentDate:Calendar

    @JvmStatic
    fun main(args: Array<String>) {
        currentDate = Calendar.getInstance()
        do {
            try {
                val (newNewspaperIds, deactivatedIds, unChangedNewspaperIds)
                        = updateSettingsIfChanged()

                refreshArticleFetchers(newNewspaperIds, deactivatedIds, unChangedNewspaperIds)
                refreshArticleDataUploaders()

                errorDelayPeriod = 0L
                errorIteration = 0L

                val now = Calendar.getInstance()
                if (now.get(Calendar.YEAR)> currentDate.get(Calendar.YEAR) ||
                        now.get(Calendar.DAY_OF_YEAR)> currentDate.get(Calendar.DAY_OF_YEAR)){
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
                    }catch (ex:Throwable){
                        ex.printStackTrace()
                        DataCoordinatorExceptionHandler.handleException(ReportGenerationException(ex))
                    }
                }

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
                    LoggerUtils.logError(ex,session)
                    Thread.sleep(getErrorDelayPeriod())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }finally {
                    if (session.isOpen){
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

    private fun refreshArticleFetchers(newNewspaperIds: List<String>, deactivatedIds: List<String>, unChangedNewspaperIds: List<String>) {
        try {
            val session = DbSessionManager.getNewSession()
            newNewspaperIds.asSequence().forEach {
                LoggerUtils.logOnConsole("newNewspaperId: ${it}")
                val newNewspaper = DatabaseUtils.findNewspaperById(session, it)!!
                session.detach(newNewspaper)
                val articleFetcher = ArticleFetcher(newNewspaper)
                ARTICLE_FETCHER_MAP.put(newNewspaper.id, articleFetcher)
                articleFetcher.start()
            }
            deactivatedIds.asSequence()
                    .forEach {
                        LoggerUtils.logOnConsole("deactivatedId: ${it}")
                        if (ARTICLE_FETCHER_MAP.containsKey(it)) {
                            ARTICLE_FETCHER_MAP.get(it)!!.interrupt()
                            ARTICLE_FETCHER_MAP.remove(it)
                        }
                    }
            unChangedNewspaperIds.asSequence().forEach {
//                println("unChangedNewspaperId: ${it}")
                if (ARTICLE_FETCHER_MAP.get(it) == null || !ARTICLE_FETCHER_MAP.get(it)!!.isAlive) {
                    ARTICLE_FETCHER_MAP.remove(it)
                    val unChangedNewspaperFromDb = DatabaseUtils.findNewspaperById(session, it)!!
                    session.detach(unChangedNewspaperFromDb)
                    val articleFetcher = ArticleFetcher(unChangedNewspaperFromDb)
                    ARTICLE_FETCHER_MAP.put(it, articleFetcher)
                    articleFetcher.start()
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

    private fun updateSettingsIfChanged(): Triple<List<String>, List<String>, List<String>> {
        try {

            val session = DbSessionManager.getNewSession()

            var settingsUpdated = false
            val settingsUpdateLogMessageBuilder = StringBuilder()

            fun setSettingsUpdated() {
                settingsUpdated = true
            }

            //For language settings current support is for adding new languages and editing current ones
            //Read current language settings from parser
            val languageMapFromParser = DataFetcherFromParser.getLanguageMap()

            //Read current language settings from own DB
            val languageMapFromDb = DatabaseUtils.getLanguageMap(session)

            val newLanguageIds = ArrayList(languageMapFromParser.keys)
            newLanguageIds.removeAll(languageMapFromDb.keys)

            if (newLanguageIds.isNotEmpty()) {
                setSettingsUpdated()
            }

            newLanguageIds.asSequence().forEach {
                settingsUpdateLogMessageBuilder.append("Language added id: ${it} | ")
                DatabaseUtils.runDbTransection(session) {
                    session.save(languageMapFromParser.get(it))
                }
            }
            languageMapFromDb.keys.asSequence()
                    .forEach {
                        if (languageMapFromParser.containsKey(it) &&
                                !languageMapFromParser.get(it)!!.equals(languageMapFromDb.get(it))) {
                            setSettingsUpdated()
                            settingsUpdateLogMessageBuilder.append("Language modified id: ${it} | ")
                            val oldLanguage = languageMapFromDb.get(it)
                            val newLanguage = languageMapFromParser.get(it)
                            oldLanguage!!.updateData(newLanguage!!)
                            DatabaseUtils.runDbTransection(session) {
                                session.update(oldLanguage)
                            }
                        }
                    }

            //same process as above
            val countriesMapFromParser = DataFetcherFromParser.getCountryMap()
            val countriesMapFromDb = DatabaseUtils.getCountriesMap(session)

            val newCountryIds = ArrayList(countriesMapFromParser.keys)
            newCountryIds.removeAll(countriesMapFromDb.keys)

            if (newCountryIds.isNotEmpty()) {
                setSettingsUpdated()
            }

            newCountryIds.asSequence().forEach {
                settingsUpdateLogMessageBuilder.append("Country added id: ${it} | ")
                DatabaseUtils.runDbTransection(session) {
                    session.save(countriesMapFromParser.get(it))
                }
            }

            countriesMapFromDb.keys.asSequence()
                    .forEach {
                        if (countriesMapFromParser.containsKey(it) &&
                                !countriesMapFromParser.get(it)!!.equals(countriesMapFromDb.get(it))) {
                            setSettingsUpdated()
                            settingsUpdateLogMessageBuilder.append("Country modified id: ${it} | ")
                            val oldCountry = countriesMapFromDb.get(it)
                            val newCountry = countriesMapFromParser.get(it)
                            oldCountry!!.updateData(newCountry!!)
                            DatabaseUtils.runDbTransection(session) {
                                session.update(oldCountry)
                            }
                        }
                    }

            //same process as above upto determining new newspapers
            val newsPaperMapFromParser = DataFetcherFromParser.getNewspaperMap()
            newsPaperMapFromParser.values.asSequence().forEach {
                it.setCountryData(ArrayList(DatabaseUtils.getCountriesMap(session).values))
                it.setLanguageData(ArrayList(DatabaseUtils.getLanguageMap(session).values))
            }
//            newsPaperMapFromParser.forEach { println("Newspaper from parser: ${it}") }

            val newsPaperMapFromDb = DatabaseUtils.getActiveNewspaperMap(session)

            val newNewspaperIds = ArrayList(newsPaperMapFromParser.keys)
            newNewspaperIds.removeAll(newsPaperMapFromDb.keys)

            val deactivatedIds = ArrayList(newsPaperMapFromDb.keys)
            deactivatedIds.removeAll(newsPaperMapFromParser.keys)

            //Save new newspapers

            if (newNewspaperIds.isNotEmpty()) {
                setSettingsUpdated()
            }
            newNewspaperIds.asSequence().forEach {
                val newspaperFromDb = DatabaseUtils.findNewspaperById(session, it)

                if (newspaperFromDb == null) {
                    settingsUpdateLogMessageBuilder.append("Newspaper added id: ${it} | ")
                    val newNewspaper = newsPaperMapFromParser.get(it)!!
                    LoggerUtils.logOnConsole("new Newspapers found : ${newNewspaper}")
                    val pages = DataFetcherFromParser.getPagesForNewspaper(newNewspaper)
                    newNewspaper.pageList = pages.toMutableList()
                    DatabaseUtils.runDbTransection(session) {
                        session.save(newNewspaper)
                        newNewspaper.pageList.forEach {
                            settingsUpdateLogMessageBuilder.append("Page added id: ${it.id} | ")
                            session.save(it)
                        }
                    }
                } else {
                    settingsUpdateLogMessageBuilder.append("Newspaper activated id: ${it} | ")
                    newspaperFromDb.active = true
                    newspaperFromDb.pageList.asSequence().forEach { it.active = false }
                    val pagesFromParser = DataFetcherFromParser.getPagesForNewspaper(newspaperFromDb)
                    pagesFromParser.asSequence().forEach {
                        if (newspaperFromDb.pageList.contains(it)) {
                            val pageFromDb = newspaperFromDb.pageList.get(newspaperFromDb.pageList.indexOf(it))
                            pageFromDb.getContentFromOther(it)
                        } else {
                            DatabaseUtils.runDbTransection(session) {
                                settingsUpdateLogMessageBuilder.append("Page added id: ${it.id} | ")
                                session.save(it)
                            }
                            newspaperFromDb.pageList.add(it)
                        }
                    }
                    DatabaseUtils.runDbTransection(session) {
                        session.update(newspaperFromDb)
                        newspaperFromDb.pageList.forEach { session.update(it) }
                    }
                }
            }

            deactivatedIds.asSequence()
                    .forEach {
                        setSettingsUpdated()
                        settingsUpdateLogMessageBuilder.append("Newspaper deactivated id: ${it} | ")
                        val deactivatedNewspaper = newsPaperMapFromDb.get(it)!!
                        deactivatedNewspaper.active = false
                        DatabaseUtils.runDbTransection(session) {
                            session.update(deactivatedNewspaper)
                        }
                    }

            val unChangedNewspaperIds = ArrayList(newsPaperMapFromParser.keys)
            unChangedNewspaperIds.removeAll(newNewspaperIds)

            unChangedNewspaperIds.asSequence().forEach {
                val unChangedNewspaper = newsPaperMapFromParser.get(it)!!
                val pagesFromParser = DataFetcherFromParser.getPagesForNewspaper(unChangedNewspaper)
                val unChangedNewspaperFromDb = newsPaperMapFromDb.get(it)!!
                if (pagesFromParser.size > unChangedNewspaperFromDb.pageList.size) {
                    setSettingsUpdated()
                    pagesFromParser.asSequence().forEach {
                        if (!unChangedNewspaperFromDb.pageList.contains(it)) {
                            DatabaseUtils.runDbTransection(session) {
                                settingsUpdateLogMessageBuilder.append("Page added id: ${it.id} | ")
                                session.save(it)
                            }
                            unChangedNewspaperFromDb.pageList.add(it)
                        }
                    }
                }
            }

            if (DatabaseUtils.getPageGroups(session).isEmpty()){
                DataFetcherFromParser.getPageGroups(session).asSequence().forEach {
                    setSettingsUpdated()
                    DatabaseUtils.runDbTransection(session) {
                        settingsUpdateLogMessageBuilder.append("Pagegroud added name: ${it.name} | ")
                        session.save(it)
                    }
                }
            }

            if (settingsUpdated) {
                val logMessage = settingsUpdateLogMessageBuilder.toString()
                DatabaseUtils.runDbTransection(session) {
                    session.save(SettingsUpdateLog(logMessage = logMessage.substring(0,logMessage.length-3))) //trailing " | " striped
                }
            }
            session.close()
            return Triple(newNewspaperIds, deactivatedIds, unChangedNewspaperIds)
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