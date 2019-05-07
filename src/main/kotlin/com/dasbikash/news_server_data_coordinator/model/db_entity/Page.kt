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
import com.google.cloud.firestore.annotation.Exclude
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.PAGE_TABLE_NAME)
data class Page(
        @Id
        var id: String=""
){

        @ManyToOne(targetEntity = Newspaper::class,fetch = FetchType.EAGER)
        @JoinColumn(name="newsPaperId")
        @Exclude
        @com.google.firebase.database.Exclude
        var newspaper: Newspaper?=null

        var active: Boolean = true

        @OneToMany(fetch = FetchType.LAZY,mappedBy = "page",targetEntity = Article::class)
        @Exclude
        @com.google.firebase.database.Exclude
        var articleList: List<Article>?=null

        var parentPageId: String?=null
        var hasData: Boolean?=null
        var hasChild: Boolean?=null
        var topLevelPage: Boolean?=null
        var name: String?=null

        @Transient
        var newsPaperId:String?=null

        fun setNewsPaperData(newspapers: List<Newspaper>){
                for (newspaper in newspapers){
                        if (newspaper.id.equals(newsPaperId)){
                                this.newspaper = newspaper
                                break
                        }
                }
                if(newspaper == null){
                        throw IllegalArgumentException()
                }
        }

        override fun toString(): String {
                return "Page(id='$id', name=$name, newsPaper=${newspaper?.name})"
        }
        fun getContentFromOther(other: Page){
                this.name=other.name
                this.active=other.active
                this.parentPageId=other.parentPageId
                this.hasData=other.hasData
                this.hasChild=other.hasChild
                this.topLevelPage=other.topLevelPage
        }
}