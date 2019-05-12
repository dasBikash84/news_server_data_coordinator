package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class FireStoreDataUtilsTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

//    @Test
//    fun testWrite(){
//        val session = DbSessionManager.getNewSession()
//        val page = DatabaseUtils.findPageById(session,"PAGE_ID_1")!!
//        println(page)
//        val article = DatabaseUtils.findLatestArticleForPage(session,page)!!
//        println(article)
//        FireStoreDataUtils.writeArticleData(articles)
//        val task =
//                FireStoreRefUtils.getArticleCollectionRef().document(article.id).set(ArticleForFB.fromArticle(article))
//
//        while (!task.isDone){}
//        println(task.get().updateTime)

//
//    }
}