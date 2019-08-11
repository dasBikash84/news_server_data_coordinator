/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.dasbikash.news_server_data_coordinator.utils.EmailUtils
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import com.dasbikash.news_server_data_coordinator.utils.RxJavaUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


object RealTimeDbAdminTaskUtils {

    private const val TOKEN_GENERATION_REQUEST_NODE = "data_coordinator_token_generation_request"
    private const val ARTICLE_UPLOADER_STATUS_CHANGE_REQUEST_NODE = "article_uploader_status_change_request"


    init {
        RealTimeDbRefUtils.getAdminTaskDataNode()
                .child(TOKEN_GENERATION_REQUEST_NODE)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError?) {}

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        RxJavaUtils.doTaskInBackGround {
                            snapshot?.let {
                                println(it)
                                val session = DbSessionManager.getNewSession()
                                it.children.asSequence().forEach {
                                    val tokenGenerationRequest = it.getValue(TokenGenerationRequest::class.java)
                                    if (tokenGenerationRequest.isValid()) {
                                        val token = AuthToken()
                                        DatabaseUtils.runDbTransection(session) { session.save(token) }
                                        EmailUtils.emailAuthTokenToAdmin(token)
                                        LoggerUtils.logOnDb("New auth token generated.", session)
                                    }
                                }
                                deleteTokenGenerationRequests()
                                session.close()
                            }
                        }
                    }
                })

        RealTimeDbRefUtils.getAdminTaskDataNode()
                .child(ARTICLE_UPLOADER_STATUS_CHANGE_REQUEST_NODE)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError?) {}

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        RxJavaUtils.doTaskInBackGround {
                            snapshot?.let {
                                println(it)
                                val session = DbSessionManager.getNewSession()

                                it.children.asSequence().forEach {
                                    val articleUploaderStatusChangeRequest =
                                            it.getValue(ArticleUploaderStatusChangeRequest::class.java)
                                    val token = session.get(AuthToken::class.java, articleUploaderStatusChangeRequest.authToken)

                                    if (token != null && !token.hasExpired()) {

                                        var status: TwoStateStatus? = null
                                        if (articleUploaderStatusChangeRequest.status.equals(TwoStateStatus.ON.name)) {
                                            status = TwoStateStatus.ON
                                        } else if (articleUploaderStatusChangeRequest.status.equals(TwoStateStatus.OFF.name)) {
                                            status = TwoStateStatus.OFF
                                        }

                                        status?.let {
                                            if (articleUploaderStatusChangeRequest.forRealTimeDb()) {
                                                val articleUploaderStatusChangeLog = ArticleUploaderStatusChangeLog(
                                                        articleDataUploaderTarget = ArticleUploadTarget.REAL_TIME_DB, status = it)
                                                DatabaseUtils.runDbTransection(session) { session.save(articleUploaderStatusChangeLog) }
                                                LoggerUtils.logOnDb("${ArticleUploadTarget.REAL_TIME_DB.name} set to ${it.name}", session)
                                            } else if (articleUploaderStatusChangeRequest.forFireStoreDb()) {
                                                val articleUploaderStatusChangeLog = ArticleUploaderStatusChangeLog(
                                                        articleDataUploaderTarget = ArticleUploadTarget.FIRE_STORE_DB, status = it)
                                                DatabaseUtils.runDbTransection(session) { session.save(articleUploaderStatusChangeLog) }
                                                LoggerUtils.logOnDb("${ArticleUploadTarget.FIRE_STORE_DB.name} set to ${it.name}", session)
                                            } else if (articleUploaderStatusChangeRequest.forMongoRestService()) {
                                                val articleUploaderStatusChangeLog = ArticleUploaderStatusChangeLog(
                                                        articleDataUploaderTarget = ArticleUploadTarget.MONGO_REST_SERVICE, status = it)
                                                DatabaseUtils.runDbTransection(session) { session.save(articleUploaderStatusChangeLog) }
                                                LoggerUtils.logOnDb("${ArticleUploadTarget.MONGO_REST_SERVICE.name} set to ${it.name}", session)
                                            }
                                        }
                                        token.makeExpired()
                                        DatabaseUtils.runDbTransection(session) { session.update(token) }
                                    }
                                }
                                deleteArticleUploaderStatusChangeRequest()
                                session.close()
                            }
                        }
                    }
                })
    }

    fun init() {}

    private fun deleteTokenGenerationRequests() {
        RealTimeDbDataUtils.clearData(RealTimeDbRefUtils.getAdminTaskDataNode().child(TOKEN_GENERATION_REQUEST_NODE))
    }

    private fun deleteArticleUploaderStatusChangeRequest() {
        RealTimeDbDataUtils.clearData(RealTimeDbRefUtils.getAdminTaskDataNode().child(ARTICLE_UPLOADER_STATUS_CHANGE_REQUEST_NODE))
    }
}