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

import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


object RealTimeDbDataCoordinatorSettingsUtils {

    private const val ARTICLE_DELETION_SETTINGS_NODE = "article_deletion_settings"
    private const val ARTICLE_UPLOAD_SETTINGS_NODE = "article_upload_settings"

    private val articleDeletionSettingsMap =
            mutableMapOf<ArticleUploadTarget,ArticleDeletionSettings>()

    private val articleUploadSettingsMap =
            mutableMapOf<ArticleUploadTarget,ArticleUploadSettings>()

    fun getArticleDeletionSettingsForTarget(articleUploadTarget: ArticleUploadTarget) =
            articleDeletionSettingsMap.get(articleUploadTarget) ?: ArticleDeletionSettings()

    fun getArticleUploadSettingsForTarget(articleUploadTarget: ArticleUploadTarget) =
            articleUploadSettingsMap.get(articleUploadTarget) ?: ArticleUploadSettings()

    fun init() {}

    init {
        RealTimeDbRefUtils.getDataCoordinatorSettingsNode()
                .child(ARTICLE_DELETION_SETTINGS_NODE)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError?) {}

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        snapshot?.let {
                            it.children.asSequence().forEach {
                                val dataSnapshot = it
                                ArticleUploadTarget.values().find { it.name == dataSnapshot.key}?.let {
                                    articleDeletionSettingsMap.put(it,dataSnapshot.getValue(ArticleDeletionSettings::class.java))
                                }
                            }
                        }
                    }
                })

        RealTimeDbRefUtils.getDataCoordinatorSettingsNode()
                .child(ARTICLE_UPLOAD_SETTINGS_NODE)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError?) {}

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        snapshot?.let {
                            it.children.asSequence().forEach {
                                val dataSnapshot = it
                                ArticleUploadTarget.values().find { it.name == dataSnapshot.key}?.let {
                                    articleUploadSettingsMap.put(it,dataSnapshot.getValue(ArticleUploadSettings::class.java))
                                }
                            }
                        }
                    }
                })
    }

}


data class ArticleDeletionSettings(
        var ARTICLE_DELETION_ROUTINE_RUNNING_HOUR:Int = DEFAULT_ARTICLE_DELETION_ROUTINE_RUNNING_HOUR,
        var DAILY_ARTICLE_DELETION_LIMIT:Int = DEFAULT_DAILY_ARTICLE_DELETION_LIMIT,
        var MAX_ARTICLE_COUNT_FOR_PAGE:Int = DEFAULT_MAX_ARTICLE_COUNT_FOR_PAGE,
        var MAX_ARTICLE_DELETION_CHUNK_SIZE:Int = DEFAULT_MAX_ARTICLE_DELETION_CHUNK_SIZE
){
    companion object{
        private const val DEFAULT_ARTICLE_DELETION_ROUTINE_RUNNING_HOUR = -1
        private const val DEFAULT_DAILY_ARTICLE_DELETION_LIMIT = 3000
        private const val DEFAULT_MAX_ARTICLE_COUNT_FOR_PAGE = 150
        private const val DEFAULT_MAX_ARTICLE_DELETION_CHUNK_SIZE = 400
    }
}


data class ArticleUploadSettings(
        var MAX_ARTICLE_AGE_DAYS:Int = DEFAULT_MAX_ARTICLE_AGE_DAYS,
        var MAX_ARTICLE_COUNT_FOR_UPLOAD:Int = DEFAULT_MAX_ARTICLE_COUNT_FOR_UPLOAD
){
    companion object{
        private const val DEFAULT_MAX_ARTICLE_AGE_DAYS = 0
        private const val DEFAULT_MAX_ARTICLE_COUNT_FOR_UPLOAD = 0
    }
}