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

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import org.hibernate.annotations.UpdateTimestamp
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.ARTICLE_DOWNLOAD_LOG_TABLE_NAME)
class ArticleDownloadLog(
        @ManyToOne(targetEntity = Page::class, fetch = FetchType.EAGER)
        @JoinColumn(name = "pageId")
        var page: Page? = null,
        downloadedArticles: List<Article>?=null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(columnDefinition = "text")
    var logMessage: String?=null
    var parents: String?=null

    init {
        if (page!=null) {
            parents = "${page?.name} | ${page?.newspaper?.name}"
        }
        if (downloadedArticles!=null) {
            val logBuilder = StringBuilder()
            downloadedArticles.asSequence().take(downloadedArticles.size - 1).forEach {
                logBuilder.append("${it.id} | ")
            }
            logBuilder.append(downloadedArticles.last().id)
            logMessage = logBuilder.toString()
        }
    }

    @Transient
    fun getArticleCount():Int{
        return logMessage?.split("|")?.size ?: 0
    }

    override fun toString(): String {
        return "ArticleDownloadLog(page=${page?.id}, id=$id, logMessage=${getArticleCount()}, parents=$parents)"
    }

}