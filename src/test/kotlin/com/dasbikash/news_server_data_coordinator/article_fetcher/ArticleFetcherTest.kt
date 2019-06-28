package com.dasbikash.news_server_data_coordinator.article_fetcher

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import org.hibernate.Session
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

internal class ArticleFetcherTest {
    lateinit var session:Session

    @BeforeEach
    fun setUp() {
        session = DbSessionManager.getNewSession()
    }

    @AfterEach
    fun tearDown() {
    }


//    @Test
//    fun run() {
//        DatabaseUtils.getNewspaperMap(session).values.asSequence().find { it.id=="NP_ID_12" }?.let {
//            println(it)
//            session.detach(it)
//            val articleFetcher = ArticleFetcher(it)
//            articleFetcher.start()
//            articleFetcher.join()
//        }
//    }
}