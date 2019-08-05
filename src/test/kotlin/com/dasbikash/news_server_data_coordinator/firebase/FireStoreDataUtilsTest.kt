package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.db_entity.Article
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
//    @Test
//    fun unwantedArticlesDeletion() {
//        val sql= "SELECT * FROM news_server_data_coordinator.articles as out_articles" +
//                " where created > '2019-07-30 06:25:35' and" +
//                " upOnFireStore and" +
//                " (select count(*) from articles as inner_articles where" +
//                " inner_articles.id=SUBSTRING_INDEX(out_articles.id,'_',1))=1" +
//                " order by created desc;"
//
//        val session = DbSessionManager.getNewSession()
//        var count = 0
//        (session.createNativeQuery(sql, Article::class.java).resultList as List<Article>).asSequence()./*take(1).*/forEach {
//            println("${count++}: $it")
//            if (FireStoreDataUtils.deleteArticleFromServer(it)){
//
//                it.upOnFirebaseDb = true
//                it.upOnMongoRest = true
//
//                it.deletedFromFireStore = true
//                it.deletedFromFirebaseDb = true
//                it.deletedFromMongoRest = true
//
//                DatabaseUtils.runDbTransection(session){session.update(it)}
//                println("Deleted: $it")
//            }
//        }
//        Thread.sleep(5000)
//    }
//    @Test
//    fun newFormatTestWrite(){
//        val reqId = "-1000095956903709058"
//        val reqId = "-1000326970240855935"
//        val reqId = "-1000594939265989143"
//        val session = DbSessionManager.getNewSession()
//        DatabaseUtils.findArticleById(session,reqId)?.let {
//            println()
//            println(it)
//            println(ArticleForFB.fromArticle2(it, emptyList()))
//            FireStoreDataUtils.writeArticleData(listOf(it),session)
//            Thread.sleep(1000)
//        }
//    }
//    @Test
//    fun newFormatTestRead(){
//        listOf<String>(/*"-9127328423538078659",*/"5628806997658721501_PAGE_ID_984").asSequence().forEach {
//            println("For id: $it")
//            val future = FireStoreRefUtils.getArticleCollectionRef().document(it).get()
//            future.get().toObject(Article::class.java)?.apply {
//                println(this.toString())
//            }
//        }
//    }
}