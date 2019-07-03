package com.dasbikash.news_server_data_coordinator.utils

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import com.dasbikash.news_server_data_coordinator.model.EntityClassNames
import com.dasbikash.news_server_data_coordinator.model.db_entity.Article
import org.hibernate.Session
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ArticleSearchResultUtilsTest {
    lateinit var session: Session

    @BeforeEach
    fun setUp() {
        session=DbSessionManager.getNewSession()
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
//
//        for (i in 1..100){
//            DatabaseUtils.getUnProcessedArticlesForSearchResult(session).asSequence().forEach {
//                ArticleSearchResultUtils.processArticleForSearchResult(session,it)
//            }
//        }


//        val hql = "select * from ${DatabaseTableNames.ARTICLE_TABLE_NAME} where processedForSearchResult=false limit 100"
//        val query =session.createNativeQuery(hql, Article::class.java)
//        (query.resultList as List<Article>).asSequence().forEach {
//            ArticleSearchResultUtils.processArticleForSearchResult(session,it)
//        }
//    }

}