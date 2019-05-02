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

package com.dasbikash.news_server_data_coordinator.model

import java.util.*
import javax.persistence.*
import kotlin.collections.ArrayList

@Entity
@Table(name = DatabaseTableNames.ARTICLE_TABLE_NAME)
data class Article(
        @Id
        var id: String="",

        @ManyToOne(targetEntity = Page::class, fetch = FetchType.EAGER)
        @JoinColumn(name = "pageId")
        var page: Page? = null,

        var title: String? = null,

        @Column(name = "articleText", columnDefinition = "text")
        var articleText: String? = null,

        @Column(columnDefinition = "text")
        var previewImageLink: String? = null,
        var publicationTime:Date? = null,
        var upOnFirebaseDb:Boolean = false,
        var upOnFireStore:Boolean = false,
        var upOnMongoRest:Boolean = false,

        @ElementCollection(targetClass = ArticleImage::class)
        @CollectionTable(name = "image_links", joinColumns = [JoinColumn(name = "articleId")])
        @Column(name = "imageLink", columnDefinition = "text")
        var imageLinkList: List<ArticleImage> = ArrayList()
)