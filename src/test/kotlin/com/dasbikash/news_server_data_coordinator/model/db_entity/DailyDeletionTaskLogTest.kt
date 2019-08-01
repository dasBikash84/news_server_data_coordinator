package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploaderForFireStoreDb
import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploaderForRealTimeDb
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import org.hibernate.Session
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class DailyDeletionTaskLogTest {
    lateinit var session: Session

    @BeforeEach
    fun setUp() {
//        session = DbSessionManager.getNewSession()
    }

    @AfterEach
    fun tearDown() {
    }

    //    @Test
//    fun schemaCheck(){
//
//    }
//    @Test
//    fun findCheck() {
//        println(DatabaseUtils.getLastDeletionTaskLogForTarget(session,ArticleUploadTarget.REAL_TIME_DB))
//    }
//    @Test
//    fun getArticleCountInDbForPageTest(){
//        DatabaseUtils.getAllPages(session).shuffled().take(10).asSequence().forEach {
//            println(it)
//            println(DatabaseUtils.getArticleCountInTargetForPage(session,it,UploadDestinationInfo.MONGO_REST_SERVICE))
//        }
//    }
//    @Test
//    fun getArticlesForDeletionTest(){
//        DatabaseUtils.getAllPages(session).filter { it.hasData!! }.take(10).asSequence().forEach {
//            println()
//            println()
//            println(it)
//            DatabaseUtils.getArticlesForDeletion(session,it,10,UploadDestinationInfo.REAL_TIME_DB)
//                    .asSequence().forEach {
//                        println(it)
//                    }
//        }
//    }

//    @Test
//    fun runDailyDeletionTaskTest(){
//        val dataUploaderForFireStoreDb =  DataUploaderForRealTimeDb()
//        if (dataUploaderForFireStoreDb.needToRunDailyArticleDeletionTask(session)) {
//            dataUploaderForFireStoreDb.runDailyArticleDeletionTask(session)
//        }
//    }
}