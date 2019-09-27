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

package com.dasbikash.news_server_data_coordinator.model.db_entity

import javax.persistence.*

@Entity
@Table(name = "article_upload_log")
class ArticleUploadLog(
        @Column(columnDefinition = "enum('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE')")
        @Enumerated(EnumType.STRING)
        var uploadTarget: ArticleUploadTarget?=null,
        uploadedArticles: List<Article>?=null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(columnDefinition = "text")
    var logMessage: String?=null
    var articleCount: Int = 0

    init {
        val logBuilder = StringBuilder()
        if (uploadedArticles!=null) {
            uploadedArticles.asSequence().take(uploadedArticles.size - 1).forEach {
                logBuilder.append("${it.id} | ")
            }
            logBuilder.append(uploadedArticles.last().id)
            logMessage = logBuilder.toString()
            articleCount = uploadedArticles.size
        }
    }
}