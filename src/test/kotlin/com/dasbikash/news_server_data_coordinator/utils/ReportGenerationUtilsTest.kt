package com.dasbikash.news_server_data_coordinator.utils

import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.*

internal class ReportGenerationUtilsTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    /*@Test
    fun prepareDailyReportTest(){
        val session = DbSessionManager.getNewSession()

        val day = Calendar.getInstance()
        day.set(Calendar.DAY_OF_MONTH,24)

        ReportGenerationUtils.prepareDailyReport(day.time,session)
        ReportGenerationUtils.emailDailyReport(day.time)
    }*/
//
//    @Test
//    fun prepareWeeklyReportTest(){
//        val session = DbSessionManager.getNewSession()
//
//        val day = Calendar.getInstance()
//        day.set(Calendar.MONTH,Calendar.MAY)
//        day.set(Calendar.DAY_OF_MONTH,15)
//
//        ReportGenerationUtils.prepareWeeklyReport(Date(),session)
//        ReportGenerationUtils.emailWeeklyReport(Date())
//    }
//
//    @Test
//    fun prepareMonthlyReportTest(){
//        val session = DbSessionManager.getNewSession()
//
//        val day = Calendar.getInstance()
//        day.set(Calendar.MONTH,Calendar.JUNE)
//        day.set(Calendar.DAY_OF_MONTH,1)
//
//        ReportGenerationUtils.prepareMonthlyReport(day.time,session)
//        ReportGenerationUtils.emailMonthlyReport(day.time)
//    }
}