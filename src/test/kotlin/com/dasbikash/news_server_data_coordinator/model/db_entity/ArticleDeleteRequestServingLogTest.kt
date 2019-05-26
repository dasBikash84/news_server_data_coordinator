package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class ArticleDeleteRequestServingLogTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
    @Test
    fun readAllDbEntry(){
        val session = DbSessionManager.getNewSession()
        DatabaseUtils.getArticleDeleteRequestServingLogCount(session).apply { println(this) }
        session.close()
    }
    @Test
    fun readAllDbEntrygCountForTarget(){
        val session = DbSessionManager.getNewSession()
        DatabaseUtils.getArticleDeleteRequestServingLogCountForTarget(session,ArticleUploadTarget.REAL_TIME_DB).apply { println(this) }
        session.close()
    }
    @Test
    fun readAllDbEntriesForTarget(){
        val session = DbSessionManager.getNewSession()
        DatabaseUtils.getArticleDeleteRequestServingLogsForTarget(session,ArticleUploadTarget.REAL_TIME_DB).apply { println(this) }
        session.close()
    }
}