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

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.google.gson.GsonBuilder
import org.hibernate.Session
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

object DataFetcherFromParser {

    private val DEFAULT_ARTICLE_REQUEST_COUNT = "article_count"
    private val NEWSPAPER_ID_PATH_PARAM = "newsPaperId"
    private val PAGE_ID_PATH_PARAM = "pageId"
    private val LAST_ARTICLE_ID_PATH_PARAM = "pageId"

    private val BASE_ADDRESS = "http://localhost:8098"
    private val LANGUAGE_SETTINGS_NODE = "languages"
    private val COUNTRY_SETTINGS_NODE = "countries"
    private val NEWS_PAPER_SETTINGS_NODE = "newspapers"
    private val PAGE_SETTINGS_NODE = "pages"
    private val LATEST_ARTICLES_FOR_PAGE_ID_NODE = "articles/page_id/{${PAGE_ID_PATH_PARAM}}/latest"
    private val ARTICLES_Before_GIVEN_ARTICLE_ID_FOR_PAGE_ID_NODE =
            "articles/page_id/{${PAGE_ID_PATH_PARAM}}/before/article_id/{${LAST_ARTICLE_ID_PATH_PARAM}}"
    private val PAGE_GROUPS_NODE = "page-groups"

    private val jerseyClient = ClientBuilder.newClient()
    private val baseTarget = jerseyClient.target(BASE_ADDRESS)
    private val languagesTarget = baseTarget.path(LANGUAGE_SETTINGS_NODE)
    private val countriesTarget = baseTarget.path(COUNTRY_SETTINGS_NODE)
    private val newsPapersTarget = baseTarget.path(NEWS_PAPER_SETTINGS_NODE)
    private val pagesTarget = baseTarget.path(PAGE_SETTINGS_NODE)
    private val latestArticlesForPageTarget = baseTarget.path(LATEST_ARTICLES_FOR_PAGE_ID_NODE)
    private val articlesBeforeGivenArticleForPageTarget = baseTarget.path(ARTICLES_Before_GIVEN_ARTICLE_ID_FOR_PAGE_ID_NODE)

    private val pagesForNpTarget = pagesTarget.path("newspaper_id/{${NEWSPAPER_ID_PATH_PARAM}}")
    private val pageGroupsTarget = baseTarget.path(PAGE_GROUPS_NODE)

    private val gson = GsonBuilder().setLenient().disableHtmlEscaping().create()

    fun getLanguageMap(): Map<String, Language> {
        val response = languagesTarget.request(MediaType.APPLICATION_JSON).get()

        val data = response.readEntity(String::class.java)!!
        val languagesFromParser = gson.fromJson(data, Languages::class.java)

        val languageMap = mutableMapOf<String, Language>()
        languagesFromParser.languages!!.asSequence().forEach {
            languageMap.put(it.id, it)
        }
        return languageMap
    }

    fun getCountryMap(): Map<String, Country> {
        val response = countriesTarget.request(MediaType.APPLICATION_JSON).get()
        val data = response.readEntity(String::class.java)!!

        val countriesFromParser = gson.fromJson(data, Countries::class.java)

        val countryMap = mutableMapOf<String, Country>()
        countriesFromParser.countries!!.asSequence()
                .forEach {
                    countryMap.put(it.name, it)
                }
        return countryMap
    }

    fun getNewspaperMap(): Map<String, Newspaper> {
        val response = newsPapersTarget.request(MediaType.APPLICATION_JSON).get()
        val data = response.readEntity(String::class.java)!!

        val newspapersFromParser = gson.fromJson(data, Newspapers::class.java)

        val newspaperMap = mutableMapOf<String, Newspaper>()
        newspapersFromParser.newspapers!!.asSequence()
                .forEach {
                    newspaperMap.put(it.id, it)
                }
        return newspaperMap
    }

    fun getPages(): List<Page> {
        val response = pagesTarget.request(MediaType.APPLICATION_JSON).get()
        val data = response.readEntity(String::class.java)!!

        val pages = gson.fromJson(data, Pages::class.java)

        return pages.pages!!
    }

    fun getPagesForNewspaper(newspaper: Newspaper): List<Page> {
        val response = pagesForNpTarget.resolveTemplate(NEWSPAPER_ID_PATH_PARAM, newspaper.id)
                .request(MediaType.APPLICATION_JSON).get()
        if (response.status == Response.Status.OK.statusCode) {
            val data = response.readEntity(String::class.java)!!

            val pages = gson.fromJson(data, Pages::class.java).pages!!
            pages.forEach { it.newspaper = newspaper }
            return pages
        } else {
            return emptyList()
        }
    }

    fun getLatestArticlesForPage(page: Page, articleCount: Int = 5): List<Article> {
        val response = latestArticlesForPageTarget
                .resolveTemplate(PAGE_ID_PATH_PARAM, page.id)
                .queryParam(DEFAULT_ARTICLE_REQUEST_COUNT, articleCount)
                .request(MediaType.APPLICATION_JSON).get()

        if (response.status == Response.Status.OK.statusCode) {
            val data = response.readEntity(String::class.java)!!
            val articles = gson.fromJson(data, Articles::class.java).articles!!//response.readEntity(Articles::class.java).articles!!
            articles.forEach { it.page = page }
            return articles
        } else {
            return emptyList()
        }
    }

    fun getArticlesBeforeGivenArticleForPage(page: Page, article: Article, articleCount: Int = 5): List<Article> {
        val response = articlesBeforeGivenArticleForPageTarget
                .resolveTemplate(PAGE_ID_PATH_PARAM, page.id)
                .resolveTemplate(LAST_ARTICLE_ID_PATH_PARAM, article.id)
                .queryParam(DEFAULT_ARTICLE_REQUEST_COUNT, articleCount)
                .request(MediaType.APPLICATION_JSON).get()

        if (response.status == Response.Status.OK.statusCode) {
            val data = response.readEntity(String::class.java)!!
            val articles = gson.fromJson(data, Articles::class.java).articles!!//response.readEntity(Articles::class.java).articles!!
            articles.forEach { it.page = page }
            return articles
        } else {
            return emptyList()
        }
    }

    fun getPageGroups(session: Session): List<PageGroup> {
        val response = pageGroupsTarget.request(MediaType.APPLICATION_JSON).get()

        if (response.status == Response.Status.OK.statusCode) {
            val data = response.readEntity(String::class.java)!!
            val pageGroups = gson.fromJson(data, PageGroups::class.java).pageGroups!!
            val pages = DatabaseUtils.getPageMap(session).values.toList()
            pageGroups.asSequence().forEach { it.setPages(pages) }
            return pageGroups
        } else {
            return emptyList()
        }
    }


}