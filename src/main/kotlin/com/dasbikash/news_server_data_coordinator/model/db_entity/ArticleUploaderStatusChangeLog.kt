package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.ARTICLE_UPLOADER_STATUS_CHANGE_LOG_TABLE_NAME)
data class ArticleUploaderStatusChangeLog (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id:Int?=null,

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "enum('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE')")
    var articleDataUploaderTarget: ArticleUploadTarget?=null,

    @UpdateTimestamp
    var created: Date?=null,

    @Column(columnDefinition = "enum('ON','OFF')")
    @Enumerated(EnumType.STRING)
    var status: TwoStateStatus?=null
)