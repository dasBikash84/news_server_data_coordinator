package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.ARTICLE_DELETE_REQUEST_TABLE_NAME)
data class ArticleDeleteRequest(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,
        var deleteRequestCount:Int?=null,
        var created:Date?= Date(),
        var served:Boolean?=false
){
    @ManyToOne(targetEntity = Page::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "pageId")
    var page: Page? = null

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE')")
    var articleUploadTarget: ArticleUploadTarget?=null

    override fun toString(): String {
        return "ArticleDeleteRequest(deleteRequestCount=$deleteRequestCount, created=$created, served=$served, page=${page?.name}, articleUploadTarget=${articleUploadTarget?.name})"
    }

}