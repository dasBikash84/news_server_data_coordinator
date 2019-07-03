package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.RESTRICTED_SEARCH_KEY_WORD_TABLE_NAME)
data class RestrictedSearchKeyWord(
        @Id
        var keyWord:String?=null,
        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false, updatable = false, insertable = false)
        var created:Date?=null
)