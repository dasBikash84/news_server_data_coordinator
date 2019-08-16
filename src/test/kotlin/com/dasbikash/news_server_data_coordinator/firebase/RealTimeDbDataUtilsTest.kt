/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.article_data_uploader.DataUploaderForFireStoreDb
import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import com.dasbikash.news_server_data_coordinator.model.db_entity.Article
import com.dasbikash.news_server_data_coordinator.model.db_entity.Page
import org.hibernate.Session
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


internal class RealTimeDbDataUtilsTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    /*@Test
    fun getNewsCategoriesForPageTest(){
        val session = DbSessionManager.getNewSession()
        val newsCategoryMap = DatabaseUtils.getNewsCategoryMap(session)
        val newsCategoryEntryMap = DatabaseUtils.getNewsCategoryEntryMap(session)

        DatabaseUtils.getAllPages(session).asSequence().forEach {
            println()
            println(it)
            println(RealTimeDbDataUtils.getNewsCategoriesForPage(it,newsCategoryMap,newsCategoryEntryMap))
        }
    }*/

    //    @Test
//    fun clearAllArticleData(){
//        RealTimeDbDataUtils.clearAllArticleData()
//    }
//    @Test
//    fun searchKeyWordsUploadTest() {
//        val session = DbSessionManager.getNewSession()
//        val futureList = mutableListOf<ApiFuture<Void>>()
//        DatabaseUtils.getSearchKeyWords(session).asSequence().forEach {
//            futureList.add(RealTimeDbRefUtils.getSearchKeyWordsNode()
//                    .child(it).setValueAsync(true))
//        }
//        futureList.asSequence().forEach {
//            while (!it.isDone){}
//        }
//    }

//    @Test
//    fun restructureArticleDataForNewsCat() {
//        val session = DbSessionManager.getNewSession()
//
//        DatabaseUtils.getAllPages(session).filter { !it.topLevelPage!! && it.id != "PAGE_ID_100" }.asSequence().forEach {
//            println()
//            println(it)
//            RealTimeDbDataUtils.clearArticleDataForPage(it)
//            println("Article data deleted.")
//            findRTDBArticleForPage(session, it).apply {
//                println("Article Count: " + this.size)
//                RealTimeDbDataUtils.writeArticleData(this)
//                println("Articledata uploaded on parents name:")
//            }
//        }

    /*println("Going to upload articledata on parents name:")
    DatabaseUtils.getAllPages(session).filter { !it.topLevelPage!! }.take(1).asSequence().forEach {
        println(it)
        return
    }*/


//    }

    /*@Test
    fun restructureNewsCategoriesArticleInfoEntry() {
        val session = DbSessionManager.getNewSession()
        var totalArticleCount = 0
        DatabaseUtils.getAllPages(session).asSequence().forEach {
            println()
            println(it)
//            RealTimeDbDataUtils.clearArticleDataForPage(it)
//            println("Article data deleted.")
            findRTDBArticleForPage(session, it).apply {
                totalArticleCount += this.size
                println("Article Count: " + this.size)
                if (this.isNotEmpty()) {
                    RealTimeDbDataUtils.writeArticleData(this, session)
                    println("NewsCategoriesArticleInfoEntry uploaded ")
                }
            }
        }
        println("totalArticleCount Count: " + totalArticleCount)
    }*/


    fun findRTDBArticleForPage(session: Session, page: Page): List<Article> {
        val sql = "SELECT * FROM ${DatabaseTableNames.ARTICLE_TABLE_NAME}" +
                " where pageId='${page.id}'" +
                " AND upOnFirebaseDb AND !deletedFromFirebaseDb"
        println(sql)
        val query = session.createNativeQuery(sql, Article::class.java)
        return query.resultList as List<Article>
    }

    //    @Test
//    fun getSearchResultMapTest(){
//        val session = DbSessionManager.getNewSession()
//        DatabaseUtils.getNewKeyWordSearchResults(session).filter { it.keyWord=="over" }.asSequence().forEach {
//            println(it.getSearchResultMap(session))
//        }
//
//    }
//    @Test
//    fun restructureKeyWordSearchResultForNewsCat() {
//        val session = DbSessionManager.getNewSession()
//        var curOffSet = 0
//        val limit = 200
//
//        do {
//            val foundKeyWordSearchResult = readKeyWordSearchResult(session, curOffSet,limit)
//            foundKeyWordSearchResult.filter { it.searchResult.isNotBlank() }.apply {
//                LoggerUtils.logOnConsole("List<KeyWordSearchResult> size: ${this.size}")
//                ArticleSearchResultUtils.writeKeyWordSearchResults(this,session)
//                LoggerUtils.logOnConsole("List<KeyWordSearchResult> uploaded")
//            }
//            curOffSet += limit
//            LoggerUtils.logOnConsole("curOffSet: $curOffSet")
//        }while (foundKeyWordSearchResult.isNotEmpty())
//    }
//
//    fun readKeyWordSearchResult(session: Session, offset:Int,limit:Int=100): List<KeyWordSearchResult> {
//        val sql = "SELECT * FROM ${DatabaseTableNames.KEY_WORD_SERACH_RESULT_TABLE_NAME} limit ${offset},${limit}"
//
//        println(sql)
//        val query = session.createNativeQuery(sql, KeyWordSearchResult::class.java)
//        return query.resultList as List<KeyWordSearchResult>
//    }
//    @Test
//    fun RealTimeDbListnerTest(){
//        RealTimeDbDataCoordinatorSettingsUtils.init()
//        RealTimeDbAdminTaskUtils.init()
//        RealTimeDbFcmUtils.init()
//        while (true){}
//    }
//    @Test
//    fun pageNotificationGenerationTest(){
//        val session = DbSessionManager.getNewSession()
//        DatabaseUtils.getAllPages(session).filter { it.id == "PAGE_ID_1" || it.id == "PAGE_ID_368" }.map{DatabaseUtils.findLatestArticleForPage(session,it)}.filter { it!=null }
//                .apply {
//                    DataUploaderForFireStoreDb().generateNotificationForUploadedArticles(session,this as List<Article>)
//                }
//        while (true){}
//    }
}