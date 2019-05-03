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

package com.dasbikash.news_server_data_coordinator.boot_strap

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.Article
import com.dasbikash.news_server_data_coordinator.settings_loader.DataFetcherFromParser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class DataFetcherFromParserTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }
    @Test
    fun readAndShowLanguages(){
        /*DataFetcherFromParser.getLanguageMap().asSequence().forEach { println("Language: ${it.name}") }
        DataFetcherFromParser.getCountryMap().asSequence().forEach { println("Country: ${it.name}") }
        DataFetcherFromParser.getNewspaperMap().asSequence().forEach { println("Newspaper: ${it.name}") }
        DataFetcherFromParser.getPages().asSequence().forEach { println("Page: ${it.name}") }*/
        val session = DbSessionManager.getNewSession()
        val page = DatabaseUtils.findPageById(session,"PAGE_ID_1080")!!
        println(DatabaseUtils.findLatestArticleForPage(session,page))
//        var lastArticle:Article?=null
        /*DataFetcherFromParser.getLatestArticlesForPage(page).asSequence()
                .forEach {
                    println(it)
                    lastArticle = it
                }
        DataFetcherFromParser.getArticlesBeforeGivenArticleForPage(page,lastArticle!!).asSequence()
                .forEach {
                    println(it)
//                    lastArticle = it
                }*/
        /*DatabaseUtils.getNewspaperMap(session)
                .filter { it.id=="NP_ID_2" }
                .forEach {
                    it.pageList?.filter { it.id == "PAGE_ID_1" }?.forEach {
                        DataFetcherFromParser.getLatestArticlesForPage(it).forEach { println(it) }
                    }
                }*/
    }
}