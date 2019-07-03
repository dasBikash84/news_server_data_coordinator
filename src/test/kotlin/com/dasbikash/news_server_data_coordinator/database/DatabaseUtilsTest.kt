package com.dasbikash.news_server_data_coordinator.database

import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget
import com.dasbikash.news_server_data_coordinator.model.db_entity.RestrictedSearchKeyWord
import org.hibernate.Session
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

internal class DatabaseUtilsTest {
    lateinit var session:Session

    @BeforeEach
    fun setUp() {
        session=DbSessionManager.getNewSession()
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
//    @Test
//    fun testGetArticleDeleteRequests(){
//        val session = DbSessionManager.getNewSession()
//        DatabaseUtils.getArticleDeleteRequests(session,ArticleUploadTarget.REAL_TIME_DB).forEach { println(it) }
//    }
//    @Test
//    fun getArticleDownloadLogWithNullPageIdTest(){
//        val session = DbSessionManager.getNewSession()
//        val pages = DatabaseUtils.getPageMap(session).values.toList()
//        var articleDownloadLogs = DatabaseUtils.getArticleDownloadLogWithNullPageId(session,5000)
//        do {
//            articleDownloadLogs.asSequence().forEach {
//                val articleDownloadLog = it
//                println(articleDownloadLog)
//                it.logMessage?.split("|")?.first()?.let {
//                    println(it)
//                    val articleId = it.trim()
//                    DatabaseUtils.findArticleById(session, articleId)?.let {
//                        articleDownloadLog.pageId = it.page!!.id
//                        println(articleDownloadLog)
//                        DatabaseUtils.runDbTransection(session) {
//                            session.update(articleDownloadLog)
//                        }
//                    }
//                }
//                println()
//                println()
//            }
//            articleDownloadLogs = DatabaseUtils.getArticleDownloadLogWithNullPageId(session,5000)
//        }while (articleDownloadLogs.isNotEmpty())
//    }
//    @Test
//    fun getArticleDownloadLogTest(){
//        val session = DbSessionManager.getNewSession()
//        var artCount = 0
//        DatabaseUtils.getArticleDownloadLog(session,50000).asSequence().forEach {
//            artCount += it.getArticleCount()
//            println(it)
//        }
//        println(artCount)
//    }
//
//    @Test
//    fun getArticleDownloadCountForPageOfYesterdayTest(){
//        val session = DbSessionManager.getNewSession()
//
//        val day = Calendar.getInstance()
//        day.set(Calendar.MONTH,Calendar.MAY)
//        day.set(Calendar.DAY_OF_MONTH,10)
//
//        DatabaseUtils.getPageMap(session).values.toList().filter { it.id.equals("PAGE_ID_368") }.first().apply {
//            println(DatabaseUtils.getArticleDownloadCountForPageOfYesterday(session,this, day.time))
//        }
//    }
//
//    @Test
//    fun getArticleDownloadCountForPageOfLastWeekTest(){
//        val session = DbSessionManager.getNewSession()
//
//        val day = Calendar.getInstance()
//        day.set(Calendar.MONTH,Calendar.MAY)
//        day.set(Calendar.DAY_OF_MONTH,15)
//
//        DatabaseUtils.getPageMap(session).values.toList().filter { it.id.equals("PAGE_ID_901") }.first().apply {
//            println(DatabaseUtils.getArticleDownloadCountForPageOfLastWeek(session,this, day.time))
//        }
//    }
//
//    @Test
//    fun getArticleDownloadCountForPageOfLastMonthTest(){
//        val session = DbSessionManager.getNewSession()
//
//        val day = Calendar.getInstance()
//        day.set(Calendar.MONTH,Calendar.JUNE)
//        day.set(Calendar.DAY_OF_MONTH,10)
//
//        DatabaseUtils.getPageMap(session).values.toList().filter { it.id.equals("PAGE_ID_780") }.first().apply {
//            println(DatabaseUtils.getArticleDownloadCountForPageOfLastMonth(session,this, day.time))
//        }
//    }
//
//    @Test
//    fun getArticleUploadCountForTargetOfYesterdayTest() {
//        val session = DbSessionManager.getNewSession()
//        DatabaseUtils.getArticleUploadCountForTargetOfYesterday(session,ArticleUploadTarget.REAL_TIME_DB,Date()).apply {
//            println("Target: ${ArticleUploadTarget.REAL_TIME_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleUploadCountForTargetOfYesterday(session,ArticleUploadTarget.FIRE_STORE_DB,Date()).apply {
//            println("Target: ${ArticleUploadTarget.FIRE_STORE_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleUploadCountForTargetOfYesterday(session,ArticleUploadTarget.MONGO_REST_SERVICE,Date()).apply {
//            println("Target: ${ArticleUploadTarget.MONGO_REST_SERVICE.name}, Count: ${this}")
//        }
//    }
//
//    @Test
//    fun getArticleUploadCountForTargetOfLastWeekTest() {
//        val session = DbSessionManager.getNewSession()
//        DatabaseUtils.getArticleUploadCountForTargetOfLastWeek(session,ArticleUploadTarget.REAL_TIME_DB,Date()).apply {
//            println("Target: ${ArticleUploadTarget.REAL_TIME_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleUploadCountForTargetOfLastWeek(session,ArticleUploadTarget.FIRE_STORE_DB,Date()).apply {
//            println("Target: ${ArticleUploadTarget.FIRE_STORE_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleUploadCountForTargetOfLastWeek(session,ArticleUploadTarget.MONGO_REST_SERVICE,Date()).apply {
//            println("Target: ${ArticleUploadTarget.MONGO_REST_SERVICE.name}, Count: ${this}")
//        }
//    }
//
//    @Test
//    fun getArticleUploadCountForTargetOfLastMonthTest() {
//        val session = DbSessionManager.getNewSession()
//
//        val day = Calendar.getInstance()
//        day.set(Calendar.MONTH,Calendar.JUNE)
//        day.set(Calendar.DAY_OF_MONTH,10)
//
//        DatabaseUtils.getArticleUploadCountForTargetOfLastMonth(session,ArticleUploadTarget.REAL_TIME_DB,day.time).apply {
//            println("Target: ${ArticleUploadTarget.REAL_TIME_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleUploadCountForTargetOfLastMonth(session,ArticleUploadTarget.FIRE_STORE_DB,day.time).apply {
//            println("Target: ${ArticleUploadTarget.FIRE_STORE_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleUploadCountForTargetOfLastMonth(session,ArticleUploadTarget.MONGO_REST_SERVICE,day.time).apply {
//            println("Target: ${ArticleUploadTarget.MONGO_REST_SERVICE.name}, Count: ${this}")
//        }
//    }
//
//    @Test
//    fun getArticleUploadCountForTargetFromBeginningTest() {
//        val session = DbSessionManager.getNewSession()
//
//        DatabaseUtils.getArticleUploadCountForTargetFromBeginning(session,ArticleUploadTarget.REAL_TIME_DB).apply {
//            println("Target: ${ArticleUploadTarget.REAL_TIME_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleUploadCountForTargetFromBeginning(session,ArticleUploadTarget.FIRE_STORE_DB).apply {
//            println("Target: ${ArticleUploadTarget.FIRE_STORE_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleUploadCountForTargetFromBeginning(session,ArticleUploadTarget.MONGO_REST_SERVICE).apply {
//            println("Target: ${ArticleUploadTarget.MONGO_REST_SERVICE.name}, Count: ${this}")
//        }
//    }
//
//    @Test
//    fun getArticleDeletionCountFromUploaderTargetTest(){
//        val session = DbSessionManager.getNewSession()
//
//        DatabaseUtils.getArticleDeletionCountFromUploaderTarget(session,ArticleUploadTarget.REAL_TIME_DB).apply {
//            println("Target: ${ArticleUploadTarget.REAL_TIME_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleDeletionCountFromUploaderTarget(session,ArticleUploadTarget.FIRE_STORE_DB).apply {
//            println("Target: ${ArticleUploadTarget.FIRE_STORE_DB.name}, Count: ${this}")
//        }
//        DatabaseUtils.getArticleDeletionCountFromUploaderTarget(session,ArticleUploadTarget.MONGO_REST_SERVICE).apply {
//            println("Target: ${ArticleUploadTarget.MONGO_REST_SERVICE.name}, Count: ${this}")
//        }
//    }
//
//    @Test
//    fun getArticleDeletionCountFromAllUploaderTargetsForPageTest(){
//        val session = DbSessionManager.getNewSession()
//
//        DatabaseUtils.getAllPages(session).toList().filter { it.id.equals("PAGE_ID_1") }.first().apply {
//            DatabaseUtils.getArticleDeletionCountFromAllUploaderTargetsForPage(session,page = this).apply {
//                this.keys.asSequence().forEach {
//                    println("${it.name}: ${this.get(it)}")
//                }
//            }
//        }
//
//    }
//    @Test
//    fun getAllRestrictedSearchKeyWord(){
//        DatabaseUtils.getAllRestrictedSearchKeyWord(session).asSequence().forEach {
//            println(it)
//        }
//    }
//    @Test
//    fun readRestrictedSearchKeyWord(){
//        session.get(RestrictedSearchKeyWord::class.java,"of")?.let {
//            println(it)
//        }
//    }
}