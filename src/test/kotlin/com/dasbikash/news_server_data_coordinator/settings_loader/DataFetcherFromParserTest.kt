package com.dasbikash.news_server_data_coordinator.settings_loader

import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class DataFetcherFromParserTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    //    @Test
//    fun testReadOfPageGroups(){
//        val session = DbSessionManager.getNewSession()
//
//        DataFetcherFromParser.getPageGroups(session).asSequence().forEach {
//            println(it)
//        }
//
//        session.close()
//    }
//    @Test
//    fun newsCategoryDataRead() {
//        val session = DbSessionManager.getNewSession()
//        DataFetcherFromParser.getNewsCategoryMap().asSequence().forEach { LoggerUtils.logOnConsole(it.toString()) }
//        DataFetcherFromParser.getNewsCategoryEntryMap(session).asSequence().forEach { LoggerUtils.logOnConsole(it.toString()) }
//    }
}