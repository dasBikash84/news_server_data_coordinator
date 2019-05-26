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
        var created:Date?= Date()
){
    @ManyToOne(targetEntity = Page::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "pageId")
    var page: Page? = null

    override fun toString(): String {
        return "ArticleDeleteRequest(id=$id, deleteRequestCount=$deleteRequestCount, created=$created, page=${page!!.name})"
    }
}