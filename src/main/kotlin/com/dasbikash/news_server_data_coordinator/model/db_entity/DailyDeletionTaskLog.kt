package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.DAILY_DELETION_TASK_LOG_TABLE_NAME)
data class DailyDeletionTaskLog(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null,

        @Column(columnDefinition = "enum('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE')")
        @Enumerated(EnumType.STRING)
        var uploadTarget: ArticleUploadTarget?=null,

        @Column(columnDefinition = "MEDIUMTEXT")
        var deletionLogMessage:String?=null,

        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false, updatable = false, insertable = false)
        var created:Date? = null
){
        companion object{
                fun getInstance(uploadTarget: ArticleUploadTarget,deletedArticleIds:List<String>):
                        DailyDeletionTaskLog{
                        return DailyDeletionTaskLog(uploadTarget=uploadTarget,
                                deletionLogMessage = deletedArticleIds.joinToString(separator=" | "))
                }
        }
}