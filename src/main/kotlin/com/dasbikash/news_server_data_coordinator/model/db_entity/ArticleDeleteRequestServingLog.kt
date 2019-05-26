package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.ARTICLE_DELETE_REQUEST_SERVING_LOG_TABLE_NAME)
data class ArticleDeleteRequestServingLog (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null
){
    @ManyToOne(targetEntity = ArticleDeleteRequest::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "articleDeleteRequestId")
    var articleDeleteRequest: ArticleDeleteRequest? = null

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE')")
    var articleUploadTarget: ArticleUploadTarget?=null

    var created: Date?= Date()

    override fun toString(): String {
        return "ArticleDeleteRequestServingLog(id=$id, articleDeleteRequest=${articleDeleteRequest?.id}, articleUploadTarget=${articleUploadTarget?.name}, created=$created)"
    }

}