package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.ARTICLE_SEARCH_RESULT_UPLOADER_LOG_TABLE_NAME)
data class ArticleSearchResultUploaderLog(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,

        var logMessage:String?=null,

        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false, updatable = false, insertable = false)
        var created:Date? = null
)