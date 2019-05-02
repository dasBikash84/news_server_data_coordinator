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
@Table(name = DatabaseTableNames.NEWSPAPER_TABLE_NAME)
data class Newspaper(
        @Id var id: String="",
        var name: String?=null,

        @ManyToOne(targetEntity = Country::class,fetch = FetchType.EAGER)
        @JoinColumn(name="countryName")
        var country: Country?=null,

        @ManyToOne(targetEntity = Language::class,fetch = FetchType.EAGER)
        @JoinColumn(name="languageId")
        var language: Language?=null,

        var active: Boolean=true,

        @OneToMany(fetch = FetchType.LAZY,mappedBy = "newspaper",targetEntity = Page::class)
        var pageList: List<Page>?=null


) {
        override fun toString(): String {
                return "Newspaper(id='$id', name=$name, country=$country, language=$language, active=$active)"
        }
}