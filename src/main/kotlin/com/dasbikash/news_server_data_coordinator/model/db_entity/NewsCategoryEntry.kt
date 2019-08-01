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
import javax.persistence.*
import javax.xml.bind.annotation.XmlRootElement

@Entity
@Table(name = DatabaseTableNames.NEWS_CATEGORY_ENTRY_ENTRY_NAME)
@XmlRootElement
data class NewsCategoryEntry(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int? = null
) {

    @Transient
    var pageId:String?=null
    @Transient
    var newsCategoryId:String?=null

    @ManyToOne(targetEntity = NewsCategory::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "newsCategoryId")
    private var newsCategory: NewsCategory? = null

    @ManyToOne(targetEntity = Page::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "pageId")
    private var page: Page? = null

    fun setPage(page: Page?){
        this.page = page
    }

    fun getPage():Page?{
        return page
    }

    fun setPageData(pages:Collection<Page>){
        pages.asSequence().forEach {
            if (it.id==pageId){
                page = it
                return
            }
        }
    }

    fun getNewsCategory():NewsCategory?{
        return newsCategory
    }

    fun setNewsCategory(newsCategory: NewsCategory?){
        this.newsCategory=newsCategory
    }

    fun setNewsCategoryData(newsCategories: Collection<NewsCategory>){
        newsCategories.asSequence().forEach {
            if (it.id==newsCategoryId){
                newsCategory = it
                return
            }
        }
    }

    override fun toString(): String {
        return "NewsCategoryEntry(id=$id, pageId=$pageId, newsCategoryId=$newsCategoryId, newsCategory=${newsCategory?.id}, page=${page?.id})"
    }

}