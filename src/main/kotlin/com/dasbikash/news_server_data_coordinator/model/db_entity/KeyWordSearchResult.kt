package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.KEY_WORD_SERACH_RESULT_TABLE_NAME)
data class KeyWordSearchResult(
        @Id
        var keyWord:String?=null,

        @Column(columnDefinition = "MEDIUMTEXT")
        var serachResult:String="",

        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false, updatable = false, insertable = false)
        var modified:Date?=null,

        var lastUploadedOnFireBaseDb:Date?=null,
        var lastUploadedOnFireStore:Date?=null,
        var lastUploadedOnMongoRestService:Date?=null
)