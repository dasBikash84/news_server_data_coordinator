package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ArticleDeleteRequestTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
    @Test
    fun readAllDbEntry(){
        val session = DbSessionManager.getNewSession()
        DatabaseUtils.getArticleDeleteRequests(session).asSequence().forEach { println(it) }
        session.close()
    }
    @Test
    fun readAllDbEntryCount(){
        val session = DbSessionManager.getNewSession()
        DatabaseUtils.getArticleDeleteRequestCount(session).apply { println("readAllDbEntryCount:${this}") }
        session.close()
    }
}