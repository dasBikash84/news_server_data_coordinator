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
import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity
@Table(name = DatabaseTableNames.ARTICLE_TABLE_NAME)
data class Article(
        @Id
        var id: String="",
        var publicationTime:Date? = null
){
        var title: String? = null
        @Column(name = "articleText", columnDefinition = "text")
        var articleText: String? = null

        @Column(columnDefinition = "text")
        var previewImageLink: String? = null

        @ManyToOne(targetEntity = Page::class, fetch = FetchType.EAGER)
        @JoinColumn(name = "pageId")
        var page: Page? = null

        var upOnFirebaseDb:Boolean = false
        var upOnFireStore:Boolean = false
        var upOnMongoRest:Boolean = false

        var deletedFromFirebaseDb:Boolean = false
        var deletedFromFireStore:Boolean = false
        var deletedFromMongoRest:Boolean = false
        var processedForSearchResult:Boolean = false

        @ElementCollection(targetClass = ArticleImage::class)
        @CollectionTable(name = "image_links", joinColumns = [JoinColumn(name = "articleId")])
        @Column(name = "imageLink", columnDefinition = "text")
        var imageLinkList: List<ArticleImage> = ArrayList()


        override fun toString(): String {
                return "Article(id='$id', title=$title,page=${page?.name}, publicationTime=${publicationTime})"
        }
        companion object{
                const val PUBLICATION_TIME_COLUMN_NAME = "publicationTime"
                const val COLUMN_NAME_FOR_ORDER_BY = "created"
        }
}