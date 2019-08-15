package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.FIREBASE_USER_INFO_SYNCHRONIZER_LOG_ENTRY_NAME)
data class FirebaseUserInfoSynchronizerLog(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id:Int=0,
        var syncedUserCount:Int=0,
        var newUserCount:Int=0,
        @Column(columnDefinition = "MEDIUMTEXT")
        var message:String="",
        @Column(nullable = false, updatable = false, insertable = false)
        var created: Date = Date()
)