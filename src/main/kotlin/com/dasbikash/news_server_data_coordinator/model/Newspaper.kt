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

import com.google.cloud.firestore.annotation.Exclude
import java.lang.IllegalArgumentException
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.NEWSPAPER_TABLE_NAME)
data class Newspaper(
        @Id var id: String="",
        var name: String?=null
) {
        @ManyToOne(targetEntity = Country::class,fetch = FetchType.EAGER)
        @JoinColumn(name="countryName")
        @Exclude
        @com.google.firebase.database.Exclude
        var country: Country?=null

        @ManyToOne(targetEntity = Language::class,fetch = FetchType.EAGER)
        @JoinColumn(name="languageId")
        @Exclude
        @com.google.firebase.database.Exclude
        var language: Language?=null

        var active: Boolean=true

        @OneToMany(fetch = FetchType.LAZY,mappedBy = "newspaper",targetEntity = Page::class
                ,cascade = arrayOf(CascadeType.ALL))
        @Exclude
        @com.google.firebase.database.Exclude
        var pageList: MutableList<Page> = mutableListOf()
        @Transient
        var countryName:String?=null
        @Transient
        var languageId:String?=null


        fun setCountryData(countries:List<Country>){
                for (country in countries){
                        if (country.name.equals(this.countryName)){
                                this.country = country
                                break
                        }
                }
                if (this.country == null){
                        throw IllegalArgumentException()
                }
        }
        fun setLanguageData(languages:List<Language>){
                for (language in languages){
                        if (language.id.equals(this.languageId)){
                                this.language = language
                                break
                        }
                }
                if (this.language == null){
                        throw IllegalArgumentException()
                }
        }
}