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

package com.dasbikash.news_server_data_coordinator.utils

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget
import com.dasbikash.news_server_data_coordinator.model.db_entity.Page
import org.hibernate.Session
import java.io.File
import java.util.*

object ReportGenerationUtils {

    fun getTableHeaderForArticleDownloadReport(): String {
        return "Sl,Page Name,Page Id,Parent Page Id,Newspaper,Article Download Count"
    }

    fun getTableHeaderForArticleUploadReport(): String {
        return "Sl,Upload Target,Article Upload Count"
    }

    fun getTableHeaderForArticleDeletionReport(): String {
        return "Sl,Page Name,Page Id,Parent Page Id,Newspaper,From Real-Time Db,From Fire-Store,From Mongo REST Service,Total"
    }

    fun prepareDailyReport(today: Date, session: Session) {

        val reportFilePath = FileUtils.getDailyReportFilePath(today)
        if (File(reportFilePath).exists()) {
            File(reportFilePath).delete()
        }
        val reportFile = File(reportFilePath)
        val yesterDay = DateUtils.getYesterDay(today)

        reportFile.appendText("Data-coordinator activity report of: ${DateUtils.getDateStringForDb(yesterDay)}\n\n")

        val uploadTargetArticleUpCountMap = mutableMapOf<ArticleUploadTarget,Int>()
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.REAL_TIME_DB,
                DatabaseUtils.getArticleUploadCountForTargetOfYesterday(session,ArticleUploadTarget.REAL_TIME_DB,today))
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.FIRE_STORE_DB,
                DatabaseUtils.getArticleUploadCountForTargetOfYesterday(session,ArticleUploadTarget.FIRE_STORE_DB,today))
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.MONGO_REST_SERVICE,
                DatabaseUtils.getArticleUploadCountForTargetOfYesterday(session,ArticleUploadTarget.MONGO_REST_SERVICE,today))

        addArticleUploadActivityReportDataToFile(reportFile,uploadTargetArticleUpCountMap,"of ${DateUtils.getDateStringForDb(yesterDay)}")

        addArticleUploadActivityReportFromBeginning(session, reportFile)
        addArticleDeletionActivityReportDataToFile(session,reportFile)

        val pages = DatabaseUtils.getAllPages(session)
        val pageArticleCountMap = mutableMapOf<Page,Int>()
        pages.asSequence().filter { it.hasData!! }.sortedBy { it.newspaper!!.name!! }.forEach {
            val articleDownloadCountOfYesterday = DatabaseUtils.getArticleDownloadCountForPageOfYesterday(session, it, today)
            pageArticleCountMap.put(it,articleDownloadCountOfYesterday)
        }

        addArticleDownloadActivityReportDataToFile(reportFile, pageArticleCountMap)
    }

    fun prepareWeeklyReport(today: Date, session: Session) {

        val reportFilePath = FileUtils.getWeeklyReportFilePath(today)

        if (File(reportFilePath).exists()) {
            File(reportFilePath).delete()
        }
        val reportFile = File(reportFilePath)

        val lastWeekFirstDay = DateUtils.getLastWeekSameDay(today)
        val lastWeekLastDay = DateUtils.getYesterDay(today)

        reportFile.appendText("Data-coordinator activity report of week: ${DateUtils.getDateStringForDb(lastWeekFirstDay)} to "+
                                "${DateUtils.getDateStringForDb(lastWeekLastDay)}\n\n")

        val uploadTargetArticleUpCountMap = mutableMapOf<ArticleUploadTarget,Int>()
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.REAL_TIME_DB,
                DatabaseUtils.getArticleUploadCountForTargetOfLastWeek(session,ArticleUploadTarget.REAL_TIME_DB,today))
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.FIRE_STORE_DB,
                DatabaseUtils.getArticleUploadCountForTargetOfLastWeek(session,ArticleUploadTarget.FIRE_STORE_DB,today))
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.MONGO_REST_SERVICE,
                DatabaseUtils.getArticleUploadCountForTargetOfLastWeek(session,ArticleUploadTarget.MONGO_REST_SERVICE,today))

        addArticleUploadActivityReportDataToFile(reportFile,uploadTargetArticleUpCountMap,
                                            " from ${DateUtils.getDateStringForDb(lastWeekFirstDay)} to ${DateUtils.getDateStringForDb(lastWeekLastDay)}")

        addArticleUploadActivityReportFromBeginning(session, reportFile)
        addArticleDeletionActivityReportDataToFile(session,reportFile)


        val pages = DatabaseUtils.getAllPages(session)
        val pageArticleCountMap = mutableMapOf<Page,Int>()
        pages.asSequence().filter { it.hasData!!}.sortedBy { it.newspaper!!.name!! }.forEach {
            val articleCountOfLastWeek = DatabaseUtils.getArticleDownloadCountForPageOfLastWeek(session, it, today)
            pageArticleCountMap.put(it,articleCountOfLastWeek)
        }

        addArticleDownloadActivityReportDataToFile(reportFile, pageArticleCountMap)
    }

    fun prepareMonthlyReport(today: Date, session: Session) {

        val reportFilePath = FileUtils.getMonthlyReportFilePath(today)

        if (File(reportFilePath).exists()) {
            File(reportFilePath).delete()
        }
        val reportFile = File(reportFilePath)
        val firstDayOfLastMonth = DateUtils.getFirstDayOfLastMonth(today)

        reportFile.appendText("Data-coordinator activity report of: ${DateUtils.getYearMonthStr(firstDayOfLastMonth)}\n\n")

        val uploadTargetArticleUpCountMap = mutableMapOf<ArticleUploadTarget,Int>()
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.REAL_TIME_DB,
                DatabaseUtils.getArticleUploadCountForTargetOfLastMonth(session,ArticleUploadTarget.REAL_TIME_DB,today))
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.FIRE_STORE_DB,
                DatabaseUtils.getArticleUploadCountForTargetOfLastMonth(session,ArticleUploadTarget.FIRE_STORE_DB,today))
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.MONGO_REST_SERVICE,
                DatabaseUtils.getArticleUploadCountForTargetOfLastMonth(session,ArticleUploadTarget.MONGO_REST_SERVICE,today))

        addArticleUploadActivityReportDataToFile(reportFile,uploadTargetArticleUpCountMap, "of ${DateUtils.getYearMonthStr(firstDayOfLastMonth)}")


        val pages = DatabaseUtils.getAllPages(session)
        val pageArticleCountMap = mutableMapOf<Page,Int>()
        pages.asSequence().filter { it.hasData!! }.sortedBy { it.newspaper!!.name!! }.forEach {
            val articleCountOfLastMonth = DatabaseUtils.getArticleDownloadCountForPageOfLastMonth(session, it, today)
            pageArticleCountMap.put(it,articleCountOfLastMonth)
        }

        addArticleDownloadActivityReportDataToFile(reportFile, pageArticleCountMap)

        addFromBeginningReport(reportFile, pages, session)
    }


    private fun addFromBeginningReport(reportFile: File, pages: List<Page>, session: Session) {
        reportFile.appendText("\n\nData-coordinator activity report from Beginning:\n\n")

        addArticleUploadActivityReportFromBeginning(session, reportFile)
        addArticleDeletionActivityReportDataToFile(session,reportFile)

        val pageArticleCountMap = mutableMapOf<Page,Int>()
        pages.asSequence().filter { it.hasData!! }.sortedBy { it.newspaper!!.name!! }.forEach {
            val articleCountFromBeginning = DatabaseUtils.getArticleCountForPage(session, it)
            pageArticleCountMap.put(it,articleCountFromBeginning)
        }
        addArticleDownloadActivityReportDataToFile(reportFile, pageArticleCountMap)
    }

    private fun addArticleUploadActivityReportFromBeginning(session: Session, reportFile: File) {
        val uploadTargetArticleUpCountMap = mutableMapOf<ArticleUploadTarget, Int>()
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.REAL_TIME_DB,
                DatabaseUtils.getArticleUploadCountForTargetFromBeginning(session, ArticleUploadTarget.REAL_TIME_DB))
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.FIRE_STORE_DB,
                DatabaseUtils.getArticleUploadCountForTargetFromBeginning(session, ArticleUploadTarget.FIRE_STORE_DB))
        uploadTargetArticleUpCountMap.put(ArticleUploadTarget.MONGO_REST_SERVICE,
                DatabaseUtils.getArticleUploadCountForTargetFromBeginning(session, ArticleUploadTarget.MONGO_REST_SERVICE))

        addArticleUploadActivityReportDataToFile(reportFile, uploadTargetArticleUpCountMap, "from beginning")
    }

    private fun getParentPageIdText(page:Page):String{
        if (page.topLevelPage ?: false){
            return "-"
        }
        return page.parentPageId!!
    }

    private fun addArticleDownloadActivityReportDataToFile(reportFile: File, pageArticleCountMap: MutableMap<Page, Int>) {
        reportFile.appendText("Article download activity report:\n\n")
        reportFile.appendText("For pages with articles:\n")
        reportFile.appendText("${getTableHeaderForArticleDownloadReport()}\n")

        var sln = 0
        var totalArticleCountOfLastMonth = 0

        pageArticleCountMap.keys.asSequence().filter { pageArticleCountMap.get(it) != 0 }.forEach {
            val articleCountOfLastMonth = pageArticleCountMap.get(it)!!
            totalArticleCountOfLastMonth += articleCountOfLastMonth
            reportFile.appendText("${++sln},${it.name},${it.id},${getParentPageIdText(it)},${it.newspaper!!.name},${articleCountOfLastMonth}\n")
        }
        reportFile.appendText(",,,,Total,${totalArticleCountOfLastMonth}\n\n")

        reportFile.appendText("For pages without any article:\n")
        reportFile.appendText("${getTableHeaderForArticleDownloadReport()}\n")
        sln = 0

        pageArticleCountMap.keys.asSequence().filter { pageArticleCountMap.get(it) == 0 }.forEach {
            val articleCountOfLastMonth = pageArticleCountMap.get(it)!!
            reportFile.appendText("${++sln},${it.name},${it.id},${getParentPageIdText(it)},${it.newspaper!!.name},${articleCountOfLastMonth}\n")
        }
        reportFile.appendText("\n\n")
    }

    private fun addArticleUploadActivityReportDataToFile(reportFile: File, uploadTargetArticleUpCountMap: MutableMap<ArticleUploadTarget, Int>,periodText:String) {
        reportFile.appendText("Article upload activity report ${periodText}:\n\n")
        reportFile.appendText("${getTableHeaderForArticleUploadReport()}\n")
        var sln = 0
        uploadTargetArticleUpCountMap.keys.asSequence().forEach {
            reportFile.appendText("${++sln},${it.name},${uploadTargetArticleUpCountMap.get(it)}\n")
        }
        reportFile.appendText("\n\n")
    }

    private fun addArticleDeletionActivityReportDataToFile(session: Session,reportFile: File){
        reportFile.appendText("Article deletion activity report:\n\n")
        reportFile.appendText("${getTableHeaderForArticleDeletionReport()}\n")

        val pages = DatabaseUtils.getAllPages(session)

        var sln = 0
        pages.asSequence().filter { it.hasData!! }.forEach {
            val articleDeletionCountFromAllUploaderTargets =
                    DatabaseUtils.getArticleDeletionCountFromAllUploaderTargetsForPage(session,it)
            val deletionCountFromRealTimeDb = articleDeletionCountFromAllUploaderTargets.get(ArticleUploadTarget.REAL_TIME_DB)!!
            val deletionCountFromFireStoreDb = articleDeletionCountFromAllUploaderTargets.get(ArticleUploadTarget.FIRE_STORE_DB)!!
            val deletionCountFromMongoRestService = articleDeletionCountFromAllUploaderTargets.get(ArticleUploadTarget.MONGO_REST_SERVICE)!!
            if (deletionCountFromRealTimeDb!=0 ||
                deletionCountFromFireStoreDb!=0 ||
                deletionCountFromMongoRestService!=0){
                reportFile.appendText("${++sln},${it.name},${it.id},${getParentPageIdText(it)},${it.newspaper!!.name}," +
                                            "${deletionCountFromRealTimeDb},${deletionCountFromFireStoreDb},${deletionCountFromMongoRestService}," +
                                            "${deletionCountFromRealTimeDb+deletionCountFromFireStoreDb+deletionCountFromMongoRestService}\n")
            }
        }
        val totalDeletionCountFromRealTimeDb = DatabaseUtils.getArticleDeletionCountFromUploaderTarget(session,ArticleUploadTarget.REAL_TIME_DB)
        val totalDeletionCountFromFireStoreDb = DatabaseUtils.getArticleDeletionCountFromUploaderTarget(session,ArticleUploadTarget.FIRE_STORE_DB)
        val totalDeletionCountFromMongoRestService= DatabaseUtils.getArticleDeletionCountFromUploaderTarget(session,ArticleUploadTarget.MONGO_REST_SERVICE)
        reportFile.appendText(",,,,Total," +
                "${totalDeletionCountFromRealTimeDb},${totalDeletionCountFromFireStoreDb},${totalDeletionCountFromMongoRestService}," +
                "${totalDeletionCountFromRealTimeDb+totalDeletionCountFromFireStoreDb+totalDeletionCountFromMongoRestService}\n")


        reportFile.appendText("\n\n")
    }

    fun emailDailyReport(today: Date) {
        val reportFilePath = FileUtils.getDailyReportFilePath(today)
        val yesterDay = DateUtils.getYesterDay(today)

        EmailUtils.sendEmail("Daily data-coordinator activity report",
                                "Daily data-coordinator activity report of ${DateUtils.getDateStringForDb(yesterDay)}",
                                        reportFilePath)
    }

    fun emailWeeklyReport(today: Date) {
        val reportFilePath = FileUtils.getWeeklyReportFilePath(today)
        val lastWeekFirstDay = DateUtils.getLastWeekSameDay(today)
        val lastWeekLastDay = DateUtils.getYesterDay(today)

        EmailUtils.sendEmail("Weekly data-coordinator activity report",
                                "Data-coordinator activity report of ${DateUtils.getDateStringForDb(lastWeekFirstDay)} to ${DateUtils.getDateStringForDb(lastWeekLastDay)}",
                                        reportFilePath)
    }

    fun emailMonthlyReport(today: Date) {
        val reportFilePath = FileUtils.getMonthlyReportFilePath(today)
        val firstDayOfLastMonth = DateUtils.getFirstDayOfLastMonth(today)

        EmailUtils.sendEmail("Monthly data-coordinator activity report",
                                "Data-coordinator activity report of ${DateUtils.getYearMonthStr(firstDayOfLastMonth)}",
                                    reportFilePath)
    }
}