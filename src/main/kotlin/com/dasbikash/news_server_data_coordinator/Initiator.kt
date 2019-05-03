@file:JvmName("com.dasbikash.news_server_data_coordinator.ArticleFetcherCoordinator")

package com.dasbikash.news_server_data_coordinator

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.Country
import com.dasbikash.news_server_data_coordinator.model.Language
import com.dasbikash.news_server_data_coordinator.model.Newspaper
import com.dasbikash.news_server_data_coordinator.settings_loader.DataFetcherFromParser

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


object ArticleFetcherCoordinator {

    private val articleFetcherMap: MutableMap<String, ArticleFetcherForNewspaper> = mutableMapOf()
    private val SETTINGS_UPDATE_ITERATION_PERIOD = 10*60*60*1000L

    @JvmStatic
    fun main(args: Array<String>) {
//        do {
            val session = DbSessionManager.getNewSession()

            //Read current language settings from parser
            val languages = DataFetcherFromParser.getLanguages()
            languages.asSequence().forEach { println("Language from parser: ${it.name}") }

            //Read current language settings from own DB
            val languagesFromDb = DatabaseUtils.getAllLanguages(session)
            val newLanguages = mutableListOf<Language>()
            languages.asSequence().forEach {
                if (!languagesFromDb.contains(it)) {
                    newLanguages.add(it)
                }
            }
            //save new languages
            if (newLanguages.isNotEmpty()) {
                println("${newLanguages.size} new languages found : ${newLanguages}")
                newLanguages.asSequence().forEach {
                    DatabaseUtils.runDbTransection(session) {
                        session.save(it)
                    }
                }
            }

            //same process as above
            val countries = DataFetcherFromParser.getCountries()
            countries.asSequence().forEach { println("Country from parser: ${it.name}") }
            val countriesFromDb = DatabaseUtils.getAllCountries(session)
            val newCountries = mutableListOf<Country>()
            countries.asSequence().forEach {
                if (!countriesFromDb.contains(it)) {
                    newCountries.add(it)
                }
            }
            if (newCountries.isNotEmpty()) {
                println("${newCountries.size} new countries found : ${newCountries}")
                newCountries.asSequence().forEach {
                    DatabaseUtils.runDbTransection(session) {
                        session.save(it)
                    }
                }
            }

            //same process as above upto determining new newspapers
            val newsPapersFromRemote = DataFetcherFromParser.getNewspapers()
            newsPapersFromRemote.asSequence().forEach {
                it.setCountryData(countries)
                it.setLanguageData(languages)
            }
            newsPapersFromRemote.forEach { println("Newspaper: ${it}") }
            val newsPapersFromDb = DatabaseUtils.getAllActiveNewspapers(session)
            val newNewspapers = mutableListOf<Newspaper>()

            newsPapersFromRemote.asSequence().forEach {
                if (!newsPapersFromDb.contains(it)) {
                    newNewspapers.add(it)
                }
            }

            //get deactivated newspaper list
            val deactivatedNewspapers = mutableListOf<Newspaper>()
            newsPapersFromDb.asSequence().forEach {
                if (!newsPapersFromRemote.contains(it)) {
                    deactivatedNewspapers.add(it)
                }
            }

            //get page list from remote and save to DB for all new newspapers
            if (newNewspapers.isNotEmpty()) {
                println("${newNewspapers.size} new Newspapers found : ${newNewspapers}")
                newNewspapers.asSequence().forEach {
                    val pages = DataFetcherFromParser.getPagesForNewspaper(it)
                    it.pageList = pages
                    DatabaseUtils.runDbTransection(session) {
                        session.save(it)
                        it.pageList?.forEach { session.save(it) }
                    }
                }
            }

            /*articleFetcherMap.keys.asSequence()
                    .forEach {
                        if (deactivatedNewspapers.map { it.id }.toList().contains(it)) {
                            articleFetcherMap.get(it)!!.interrupt()
                            articleFetcherMap.remove(it)
                        }
                    }

            newsPapersFromRemote.asSequence()
                    .forEach {
                        if (!articleFetcherMap.keys.contains(it.id)){
                            val articleFetcher = ArticleFetcherForNewspaper(it,it.pageList!!)
                            articleFetcherMap.put(it.id, articleFetcher)
                            articleFetcher.start()
                        }else if(!articleFetcherMap.get(it.id)!!.isAlive){
                            articleFetcherMap.remove(it.id)
                            val articleFetcher = ArticleFetcherForNewspaper(it,it.pageList!!)
                            articleFetcherMap.put(it.id, articleFetcher)
                            articleFetcher.start()
                        }
                    }*/
            session.close()
//            try {
//                Thread.sleep(SETTINGS_UPDATE_ITERATION_PERIOD)
//            } catch (ex: InterruptedException) {
//                ex.printStackTrace()
//            }
//        }while (true)
    }
}

