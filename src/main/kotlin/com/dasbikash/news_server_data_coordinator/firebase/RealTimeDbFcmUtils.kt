package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.exceptions.NotificationGenerationException
import com.dasbikash.news_server_data_coordinator.exceptions.handlers.DataCoordinatorExceptionHandler
import com.dasbikash.news_server_data_coordinator.model.db_entity.Article
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message


object RealTimeDbFcmUtils {

    private const val ONE_MINUTE_MS = 60 * 1000L
    private const val ONE_HOUR_MS = 60 * ONE_MINUTE_MS
    private const val ONE_DAY_MS = 24 * ONE_HOUR_MS
    private const val MAX_DELAY_FOR_FCM_NOTIFICATION_REQ = 10 * ONE_MINUTE_MS

    private const val FCM_PAGE_ID_KEY = "FCM_PAGE_ID"
    private const val FCM_ARTICLE_ID_KEY = "FCM_ARTICLE_ID"

    private const val NOTIFICATION_TOPIC_NAME = "article_broadcast"

    fun init() {}

    init {
        RealTimeDbRefUtils.getFcmNotificationGenReqRef()
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError?) {}

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        snapshot?.let {
                            if (it.hasChildren()) {
                                val session = DbSessionManager.getNewSession()
                                it.children.asSequence().forEach {
                                    val dataSnapshot = it
                                    val fcmNotificationGenerationRequest =
                                            it.getValue(FcmNotificationGenerationRequest::class.java)
                                    LoggerUtils.logOnConsole(fcmNotificationGenerationRequest.toString())
                                    if (fcmNotificationGenerationRequest.articleId != null &&
                                            fcmNotificationGenerationRequest.timeStamp != null &&
                                            (System.currentTimeMillis() - fcmNotificationGenerationRequest.timeStamp!!)
                                            < MAX_DELAY_FOR_FCM_NOTIFICATION_REQ) {
                                        DatabaseUtils.findArticleById(session, fcmNotificationGenerationRequest.articleId!!)?.let {
                                            try {
                                                generateNotificationForArticle(it)
                                            } catch (ex: Throwable) {
                                                DataCoordinatorExceptionHandler.handleException(NotificationGenerationException(ex.message, ex))
                                            }
                                            val futureTask = dataSnapshot.getRef().setValueAsync(null)
                                            while (!futureTask.isDone) {
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
    }

    private fun getDataPayloadForArticle(article: Article): Map<String, String> {
        val dataPayLoad = mutableMapOf<String, String>()
        dataPayLoad.put(FCM_ARTICLE_ID_KEY, article.id)
        val pageId = when {
            article.page!!.topLevelPage!! -> article.page!!.id
            else -> article.page!!.parentPageId!!
        }
        dataPayLoad.put(FCM_PAGE_ID_KEY, pageId)
        return dataPayLoad.toMap()
    }

    private fun generateNotificationForArticle(article: Article) {
        val title = article.page!!.name + " | " + article.page!!.newspaper!!.name!!
        val body = article.title!!

        val androidNotification =
                AndroidNotification.builder()
                        .setTitle(title).setBody(body)
                        .setSound("default")
                        .build()

        val androidConfig =
                AndroidConfig.builder().setNotification(androidNotification)
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setTtl(ONE_DAY_MS)
                        .putAllData(getDataPayloadForArticle(article))
                        .build()
        val message = Message.builder().setAndroidConfig(androidConfig)
                .setTopic(NOTIFICATION_TOPIC_NAME)
                .build()
        FirebaseMessaging.getInstance().send(message)
    }
}

data class FcmNotificationGenerationRequest(
        var articleId: String? = null,
        var timeStamp: Long? = null
)