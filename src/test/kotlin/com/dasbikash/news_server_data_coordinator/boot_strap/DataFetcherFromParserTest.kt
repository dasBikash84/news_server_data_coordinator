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
import com.dasbikash.news_server_data_coordinator.settings_loader.DataFetcherFromParser
import com.dasbikash.news_server_data_coordinator.settings_loader.RestEndPointsForSettingsData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
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
        /*DataFetcherFromParser.getLanguages().asSequence().forEach { println("Language: ${it.name}") }
        DataFetcherFromParser.getCountries().asSequence().forEach { println("Country: ${it.name}") }
        DataFetcherFromParser.getNewspapers().asSequence().forEach { println("Newspaper: ${it.name}") }
        DataFetcherFromParser.getPages().asSequence().forEach { println("Page: ${it.name}") }*/
        val session = DbSessionManager.getNewSession()
        DatabaseUtils.getAllActiveNewspapers(session)
                .filter { it.id=="NP_ID_2" }
                .forEach {
                    it.pageList?.filter { it.id == "PAGE_ID_1" }?.forEach {
                        DataFetcherFromParser.getLatestArticlesForPage(it).forEach { println(it) }
                    }
                }
    }
}