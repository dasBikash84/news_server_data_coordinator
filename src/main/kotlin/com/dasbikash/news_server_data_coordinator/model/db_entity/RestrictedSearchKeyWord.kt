package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = DatabaseTableNames.RESTRICTED_SEARCH_KEY_WORD_TABLE_NAME)
data class RestrictedSearchKeyWord(
        @Id
        var keyword:String?=null,
        var created:Date?=null
)