package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ArticleUploaderStatusChangeLogTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

//    @Test
//    fun testStatusRead(){
//        val session = DbSessionManager.getNewSession()
//        println(DatabaseUtils.getArticleUploaderStatus(session,ArticleUploadTarget.REAL_TIME_DB))
//        println(DatabaseUtils.getArticleUploaderStatus(session,ArticleUploadTarget.FIRE_STORE_DB))
//        println(DatabaseUtils.getArticleUploaderStatus(session,ArticleUploadTarget.MONGO_REST_SERVICE))
//    }
}