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

import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.PAGE_TABLE_NAME)
data class Page(
        @Id
        var id: String="",
        var name: String?=null,

        @ManyToOne(targetEntity = Newspaper::class,fetch = FetchType.EAGER)
        @JoinColumn(name="newsPaperId")
        var newspaper: Newspaper?=null,

        var parentPageId: String?=null,
        var hasData: Boolean?=null,
        var hasChild: Boolean?=null,
        var topLevelPage: Boolean?=null,

        var active: Boolean = true,

        @OneToMany(fetch = FetchType.LAZY,mappedBy = "page",targetEntity = Article::class)
        var articleList: List<Article>?=null

)