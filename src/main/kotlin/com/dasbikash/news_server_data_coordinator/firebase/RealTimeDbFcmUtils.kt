package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.exceptions.NotificationGenerationException
import com.dasbikash.news_server_data_coordinator.exceptions.handlers.DataCoordinatorExceptionHandler
import com.dasbikash.news_server_data_coordinator.model.db_entity.Article
import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleNotificationGenerationLog
import com.dasbikash.news_server_data_coordinator.model.db_entity.AuthToken
import com.dasbikash.news_server_data_coordinator.model.db_entity.Page
import com.dasbikash.news_server_data_coordinator.utils.DateUtils
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import com.dasbikash.news_server_data_coordinator.utils.RxJavaUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import java.util.*


object RealTimeDbFcmUtils {

    private const val ONE_MINUTE_MS = 60 * 1000L
    private const val ONE_HOUR_MS = 60 * ONE_MINUTE_MS
    private const val ONE_DAY_MS = 24 * ONE_HOUR_MS
    private const val MAX_DELAY_FOR_FCM_NOTIFICATION_REQ = 10 * ONE_MINUTE_MS

    private const val LAST_HOUR_OF_NOTIFICATION = 22
    private const val FIRST_HOUR_OF_NOTIFICATION = 7

    private const val FCM_PAGE_ID_KEY = "FCM_PAGE_ID"
    private const val FCM_ARTICLE_ID_KEY = "FCM_ARTICLE_ID"

    private const val NOTIFICATION_TOPIC_NAME = "article_broadcast"

    fun init() {}

    init {
        RealTimeDbRefUtils.getFcmNotificationGenReqRef()
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError?) {}

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        RxJavaUtils.doTaskInBackGround {
                            snapshot?.let {
                                println(it)
                                if (it.hasChildren()) {
                                    val session = DbSessionManager.getNewSession()
                                    it.children.asSequence().forEach {
                                        val fcmNotificationGenerationRequest =
                                                it.getValue(FcmNotificationGenerationRequest::class.java)
                                        LoggerUtils.logOnConsole(fcmNotificationGenerationRequest.toString())
                                        if (fcmNotificationGenerationRequest.articleId != null &&
                                                fcmNotificationGenerationRequest.authToken != null) {

                                            val token =
                                                    session.get(AuthToken::class.java, fcmNotificationGenerationRequest.authToken)

                                            if (token != null && !token.hasExpired()) {
                                                DatabaseUtils.findArticleById(session, fcmNotificationGenerationRequest.articleId!!)?.let {
                                                    try {
                                                        val parentPage = when{
                                                            it.page!!.topLevelPage!! -> it.page!!
                                                            else -> DatabaseUtils.findPageById(session,it.page!!.parentPageId!!)!!
                                                        }
                                                        DatabaseUtils.runDbTransection(session) {
                                                            session.save(ArticleNotificationGenerationLog(page = parentPage, article = it))
                                                        }
                                                        session.detach(it)
                                                        session.detach(parentPage)
                                                        generateNotificationForArticle(it,parentPage)
                                                        LoggerUtils.logOnDb("Notification generated for articleId: ${it.id}",session)
                                                    } catch (ex: Throwable) {
                                                        DataCoordinatorExceptionHandler.handleException(NotificationGenerationException(ex.message, ex))
                                                    }
                                                }
                                            }else{
                                                LoggerUtils.logOnDb(
                                                        "Notification generation failure for articleId: ${fcmNotificationGenerationRequest.articleId}",session)
                                            }
                                        }
                                    }
                                    RealTimeDbDataUtils.clearData(it.ref)
                                }
                            }
                        }
                    }
                })
    }

    private fun getDataPayloadForArticle(article: Article, parentPage: Page): Map<String, String> {
        val dataPayLoad = mutableMapOf<String, String>()
        dataPayLoad.put(FCM_ARTICLE_ID_KEY, article.id)
        dataPayLoad.put(FCM_PAGE_ID_KEY, parentPage.id)
        return dataPayLoad.toMap()
    }

    fun generateNotificationForArticle(article: Article, parentPage: Page,
                                       notificationTopic:String=NOTIFICATION_TOPIC_NAME, timeToLive:Long=3*DateUtils.ONE_HOUR_IN_MS) {
        val title = parentPage.name + " | " + article.page!!.newspaper!!.name!!
        val body = article.title!!

        val androidNotificationBuilder = AndroidNotification.builder().setTitle(title).setBody(body)

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour <= LAST_HOUR_OF_NOTIFICATION && currentHour >= FIRST_HOUR_OF_NOTIFICATION ){
            androidNotificationBuilder.setSound("default")
        }

        val androidConfig =
                AndroidConfig.builder().setNotification(androidNotificationBuilder.build())
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setTtl(timeToLive)
                        .putAllData(getDataPayloadForArticle(article,parentPage))
                        .build()
        val message = Message.builder().setAndroidConfig(androidConfig)
                .setTopic(notificationTopic)
                .build()
        FirebaseMessaging.getInstance().send(message)
    }
}

data class FcmNotificationGenerationRequest(
        var articleId: String? = null,
        var authToken: String? = null
)