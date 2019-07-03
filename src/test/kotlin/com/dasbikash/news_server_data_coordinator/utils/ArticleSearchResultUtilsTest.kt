package com.dasbikash.news_server_data_coordinator.utils

import com.dasbikash.news_server_data_coordinator.article_search_result_processor.ArticleSearchReasultProcessor
import com.dasbikash.news_server_data_coordinator.article_search_result_processor.ArticleSearchResultUtils
import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.firebase.RealTimeDbDataUtils
import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import com.dasbikash.news_server_data_coordinator.model.EntityClassNames
import com.dasbikash.news_server_data_coordinator.model.db_entity.Article
import com.dasbikash.news_server_data_coordinator.model.db_entity.KeyWordSearchResult
import org.hibernate.Session
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ArticleSearchResultUtilsTest {
    lateinit var session: Session

    @BeforeEach
    fun setUp() {
        session = DbSessionManager.getNewSession()
    }

    @AfterEach
    fun tearDown() {
    }

//    @Test
//    fun checkIfKeyWordRestricted() {
//        println(ArticleSearchResultUtils.checkIfKeyWordRestricted(session,"কিন্তু"))
//    }

    //    @Test
//    fun processArticleForSearchResult(){
//        var count = 0
//        for (i in 1..5){
//            DatabaseUtils.getUnProcessedArticlesForSearchResult(session).asSequence().forEach {
//                ArticleSearchResultUtils.processArticleForSearchResult(session,it)
//                count++
//            }
//            println("${count} articles processed for search result.")
//        }
//
//
//            val hql = "select * from ${DatabaseTableNames.ARTICLE_TABLE_NAME} where processedForSearchResult=false limit 100"
//        val query =session.createNativeQuery(hql, Article::class.java)
//        (query.resultList as List<Article>).asSequence().forEach {
//            ArticleSearchResultUtils.processArticleForSearchResult(session,it)
//        }
//    }
//    @Test
//    fun KeyWordSearchResultReadTest() {
//
//        DatabaseUtils.getNewKeyWordSearchResults(session,10).let {
//            ArticleSearchResultUtils.writeKeyWordSearchResults(it,session)
//        }
//
//    }
//    @Test
//    fun testRunOfArticleSearchReasultProcessor(){
//        ArticleSearchReasultProcessor.getInstance()?.let {
//            it.start()
//            it.join()
//        }
//    }
}