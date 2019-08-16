package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.ARTICLE_NOTIFICATION_GENERATION_LOG_TABLE_NAME)
data class ArticleNotificationGenerationLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @ManyToOne(targetEntity = Page::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "parentPageId")
    var page: Page? = null,

    @ManyToOne(targetEntity = Article::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "articleId")
    var article: Article? = null,
    @Column(insertable = false,updatable = false,nullable = false)
    var created: Date?=null
)