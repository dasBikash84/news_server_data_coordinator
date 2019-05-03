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

package com.dasbikash.news_server_data_coordinator.settings_loader

import com.dasbikash.news_server_data_coordinator.model.*
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

object DataFetcherFromParser {

    val BASE_ADDRESS = "http://localhost:8098"
    val LANGUAGE_SETTINGS_NODE = "languages"
    val COUNTRY_SETTINGS_NODE = "countries"
    val NEWS_PAPER_SETTINGS_NODE = "newspapers"
    val PAGE_SETTINGS_NODE = "pages"
    val LATEST_ARTICLES_FOR_PAGE_ID_NODE = "articles/page_id/{pageId}/latest"
    val ARTICLES_AFTER_GIVEN_ARTICLE_ID_FOR_PAGE_ID_NODE =
            "articles/page_id/{pageId}/after/article_id/{lastArticleId}"

    val jerseyClient = ClientBuilder.newClient()
    val baseTarget = jerseyClient.target(BASE_ADDRESS)
    val languagesTarget = baseTarget.path(LANGUAGE_SETTINGS_NODE)
    val countriesTarget = baseTarget.path(COUNTRY_SETTINGS_NODE)
    val newsPapersTarget = baseTarget.path(NEWS_PAPER_SETTINGS_NODE)
    val pagesTarget = baseTarget.path(PAGE_SETTINGS_NODE)
    val latestArticlesForPageTarget = baseTarget.path(LATEST_ARTICLES_FOR_PAGE_ID_NODE)
    val articlesAfterGivenArticleForPageTarget = baseTarget.path(ARTICLES_AFTER_GIVEN_ARTICLE_ID_FOR_PAGE_ID_NODE)



    val pagesForNpTarget = pagesTarget.path("newspaper_id/{newsPaperId}")

    fun getLanguageMap():Map<String,Language>{
        val response = languagesTarget.request(MediaType.APPLICATION_JSON).get()
        val languagesFromParser = response.readEntity(Languages::class.java)
        val languageMap = mutableMapOf<String,Language>()
        languagesFromParser.languages!!.asSequence().forEach {
            languageMap.put(it.id,it)
        }
        return languageMap
    }

    fun getCountryMap():Map<String,Country>{
        val response = countriesTarget.request(MediaType.APPLICATION_JSON).get()
        val countriesFromParser = response.readEntity(Countries::class.java)
        val countryMap = mutableMapOf<String,Country>()
        countriesFromParser.countries!!.asSequence()
                .forEach {
                    countryMap.put(it.name,it)
                }
        return countryMap
    }

    fun getNewspaperMap():Map<String,Newspaper>{
        val response = newsPapersTarget.request(MediaType.APPLICATION_JSON).get()
        val newspapersFromParser = response.readEntity(Newspapers::class.java)
        val newspaperMap = mutableMapOf<String,Newspaper>()
        newspapersFromParser.newspapers!!.asSequence()
                .forEach {
                    newspaperMap.put(it.id,it)
                }
        return newspaperMap
    }

    fun getPages():List<Page>{
        val response = pagesTarget.request(MediaType.APPLICATION_JSON).get()
        val pages = response.readEntity(Pages::class.java)
        return pages.pages!!
    }

    fun getPagesForNewspaper(newspaper: Newspaper):List<Page>{
        val response = pagesForNpTarget.resolveTemplate("newsPaperId",newspaper.id)
                                            .request(MediaType.APPLICATION_JSON).get()
        if (response.status == Response.Status.OK.statusCode){
            val pages = response.readEntity(Pages::class.java).pages!!
            pages.forEach { it.newspaper = newspaper }
            return pages
        }else{
            return emptyList()
        }
    }
    fun getLatestArticlesForPage(page: Page):List<Article>{
        println("page: ${page.name}")
        val response = latestArticlesForPageTarget.resolveTemplate("pageId",page.id)
                                        .request(MediaType.APPLICATION_JSON).get()

        if (response.status == Response.Status.OK.statusCode){
            val articles = response.readEntity(Articles::class.java).articles!!
            articles.forEach { it.page = page }
            return articles
        }else{
            return emptyList()
        }
    }


}