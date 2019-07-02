package com.dasbikash.news_server_data_coordinator.article_data_uploader

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import com.dasbikash.news_server_data_coordinator.utils.ReportGenerationUtils
import org.hibernate.Session
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class UploadDestinationInfoTest {

    lateinit var session: Session

    @BeforeEach
    fun setUp() {
        session = DbSessionManager.getNewSession()
    }

    @AfterEach
    fun tearDown() {
    }

//    @Test
//    fun getArticleDeletionCountFromUploaderTargetForPageTest(){
//
//        val today = Date()
//
//        LoggerUtils.logOnConsole("Starting daily data-coordinator activity report generation.")
//        ReportGenerationUtils.prepareDailyReport(today, session)
//        LoggerUtils.logOnConsole("Daily data-coordinator activity report generated.")
//        ReportGenerationUtils.emailDailyReport(today)
//        LoggerUtils.logOnConsole("Daily data-coordinator activity report distributed.")
//
//
//    }
}