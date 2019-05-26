package com.dasbikash.news_server_data_coordinator.database

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal class DatabaseUtilsTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    //    @Test
//    fun getArticlesForDeletionTest(){
//        val session = DbSessionManager.getNewSession()
//        val articleDeleteRequests = DatabaseUtils.getArticleDeleteRequests(session)
//        val page = DatabaseUtils.getPageMap(session).values.find { it.id.equals("PAGE_ID_776") }
//        DatabaseUtils.getArticlesForDeletion(session,articleDeleteRequests.get(0).page!!,articleDeleteRequests.get(0).deleteRequestCount!!,ArticleUploadTarget.FIRE_STORE_DB)
//        DatabaseUtils.getArticlesForDeletion(session,page!!,articleDeleteRequests.get(0).deleteRequestCount!!,ArticleUploadTarget.REAL_TIME_DB)
//                .asSequence().forEach {
//                    println(it)
//                }
//        DatabaseUtils.getArticlesForDeletion(session,articleDeleteRequests.get(0).page!!,articleDeleteRequests.get(0).deleteRequestCount!!,ArticleUploadTarget.MONGO_REST_SERVICE)
//    }
//    @Test
//    fun markArticleAsDeletedFromDataStoreTest() {
//        val session = DbSessionManager.getNewSession()
//        DatabaseUtils.markArticleAsDeletedFromDataStore(session, Article(id = "-1000095956903709058"),ArticleUploadTarget.FIRE_STORE_DB)
//    }
}