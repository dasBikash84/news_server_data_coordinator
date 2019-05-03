@file:JvmName("com.dasbikash.news_server_data_coordinator.ArticleFetcherCoordinator")

package com.dasbikash.news_server_data_coordinator

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
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
    private val SETTINGS_UPDATE_ITERATION_PERIOD = 10 * 60 * 60 * 1000L

    @JvmStatic
    fun main(args: Array<String>) {
        do {
            val session = DbSessionManager.getNewSession()

            //For language settings current support is for adding new languages and editing current ones
            //Read current language settings from parser
            val languageMapFromParser = DataFetcherFromParser.getLanguageMap()
//        languageMapFromParser.values.asSequence().forEach { println("Language from parser: ${it}") }

            //Read current language settings from own DB
            val languageMapFromDb = DatabaseUtils.getLanguageMap(session)
//        languageMapFromDb.values.asSequence().forEach { println("Language from DB: ${it}") }

            val newLanguageIds = ArrayList(languageMapFromParser.keys)
            newLanguageIds.removeAll(languageMapFromDb.keys)

            newLanguageIds.asSequence().forEach {
                DatabaseUtils.runDbTransection(session) {
                    session.save(languageMapFromParser.get(it))
                }
            }
            languageMapFromDb.keys.asSequence()
                    .forEach {
                        if (languageMapFromParser.containsKey(it) &&
                                !languageMapFromParser.get(it)!!.equals(languageMapFromDb.get(it))) {
//                        println("Language modified: ${languageMapFromParser.get(it)}")
                            val oldLanguage = languageMapFromDb.get(it)
                            val newLanguage = languageMapFromParser.get(it)
                            oldLanguage!!.updateData(newLanguage!!)
                            DatabaseUtils.runDbTransection(session) {
                                session.update(oldLanguage)
                            }
                        }
                    }

            //same process as above
            val countriesMapFromParser = DataFetcherFromParser.getCountryMap()
//        countriesMapFromParser.values.asSequence().forEach { println("Country from parser: ${it}") }
            val countriesMapFromDb = DatabaseUtils.getCountriesMap(session)
//        countriesMapFromDb.values.asSequence().forEach { println("Country from DB: ${it}") }

            val newCountryIds = ArrayList(countriesMapFromParser.keys)
            newCountryIds.removeAll(countriesMapFromDb.keys)

            newCountryIds.asSequence().forEach {
                DatabaseUtils.runDbTransection(session) {
                    session.save(countriesMapFromParser.get(it))
                }
            }

            countriesMapFromDb.keys.asSequence()
                    .forEach {
                        if (countriesMapFromParser.containsKey(it) &&
                                !countriesMapFromParser.get(it)!!.equals(countriesMapFromDb.get(it))) {
                            val oldCountry = countriesMapFromDb.get(it)
//                        println("Country modified: oldCountry")
                            val newCountry = countriesMapFromParser.get(it)
                            oldCountry!!.updateData(newCountry!!)
                            DatabaseUtils.runDbTransection(session) {
                                session.update(oldCountry)
                            }
                        }
                    }

            //same process as above upto determining new newspapers
            val newsPaperMapFromParser = DataFetcherFromParser.getNewspaperMap()
            newsPaperMapFromParser.values.asSequence().forEach {
                it.setCountryData(ArrayList(DatabaseUtils.getCountriesMap(session).values))
                it.setLanguageData(ArrayList(DatabaseUtils.getLanguageMap(session).values))
            }
            newsPaperMapFromParser.forEach { println("Newspaper from parser: ${it}") }

            val newsPaperMapFromDb = DatabaseUtils.getNewspaperMap(session)

            val newNewspaperIds = ArrayList(newsPaperMapFromParser.keys)
            newNewspaperIds.removeAll(newsPaperMapFromDb.keys)

            val deactivatedIds = ArrayList(newsPaperMapFromDb.keys)
            deactivatedIds.removeAll(newsPaperMapFromParser.keys)

            //Save new newspapers
            newNewspaperIds.asSequence().forEach {
                val newspaperFromDb = DatabaseUtils.findNewspaperById(session, it)

                if (newspaperFromDb == null) {
                    val newNewspaper = newsPaperMapFromParser.get(it)!!
                    println("new Newspapers found : ${newNewspaper}")
                    val pages = DataFetcherFromParser.getPagesForNewspaper(newNewspaper)
                    newNewspaper.pageList = pages.toMutableList()
                    DatabaseUtils.runDbTransection(session) {
                        session.save(newNewspaper)
                        newNewspaper.pageList.forEach { session.save(it) }
                    }
                } else {
                    newspaperFromDb.active = true
                    newspaperFromDb.pageList.asSequence().forEach { it.active = false }
                    val pagesFromParser = DataFetcherFromParser.getPagesForNewspaper(newspaperFromDb)
                    pagesFromParser.asSequence().forEach {
                        if (newspaperFromDb.pageList.contains(it)) {
                            newspaperFromDb.pageList.get(newspaperFromDb.pageList.indexOf(it)).active = true
                        } else {
                            DatabaseUtils.runDbTransection(session) {
                                session.save(it)
                            }
                            newspaperFromDb.pageList.add(it)
                        }
                    }
                    DatabaseUtils.runDbTransection(session) {
                        session.update(newspaperFromDb)
                        newspaperFromDb.pageList.forEach { session.update(it) }
                    }
                }
                val newNewspaper = DatabaseUtils.findNewspaperById(session, it)!!

                val articleFetcher = ArticleFetcherForNewspaper(newNewspaper, newNewspaper.pageList)
                articleFetcherMap.put(newNewspaper.id, articleFetcher)
                articleFetcher.start()
            }

            deactivatedIds.asSequence()
                    .forEach {
                        val deactivatedNewspaper = newsPaperMapFromDb.get(it)!!
                        deactivatedNewspaper.active = false
                        DatabaseUtils.runDbTransection(session) {
                            session.update(deactivatedNewspaper)
                        }
                        articleFetcherMap.get(it)!!.interrupt()
                        articleFetcherMap.remove(it)
                    }

            val unChangedNewspaperIds = ArrayList(newsPaperMapFromParser.keys)
            unChangedNewspaperIds.removeAll(newNewspaperIds)

            unChangedNewspaperIds.asSequence().forEach {
                val unChangedNewspaper = newsPaperMapFromParser.get(it)!!
                val pagesFromParser = DataFetcherFromParser.getPagesForNewspaper(unChangedNewspaper)
                val unChangedNewspaperFromDb = newsPaperMapFromDb.get(it)!!
                if (pagesFromParser.size > unChangedNewspaperFromDb.pageList.size) {
                    articleFetcherMap.get(it)!!.interrupt()
                    articleFetcherMap.remove(it)
                    pagesFromParser.asSequence().forEach {
                        if (!unChangedNewspaperFromDb.pageList.contains(it)) {
                            DatabaseUtils.runDbTransection(session) {
                                session.save(it)
                            }
                            unChangedNewspaperFromDb.pageList.add(it)
                        }
                    }
                    val articleFetcher = ArticleFetcherForNewspaper(unChangedNewspaperFromDb, unChangedNewspaperFromDb.pageList)
                    articleFetcherMap.put(unChangedNewspaperFromDb.id, articleFetcher)
                    articleFetcher.start()
                } else {
                    if (!articleFetcherMap.get(it)!!.isAlive) {
                        articleFetcherMap.remove(it)
                        val unChangedNewspaperFromDb = newsPaperMapFromDb.get(it)!!
                        val articleFetcher = ArticleFetcherForNewspaper(unChangedNewspaperFromDb, unChangedNewspaperFromDb.pageList)
                        articleFetcherMap.put(unChangedNewspaperFromDb.id, articleFetcher)
                        articleFetcher.start()
                    }
                }
            }
            session.close()

            try {
                Thread.sleep(SETTINGS_UPDATE_ITERATION_PERIOD)
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
            }
        } while (true)
    }
}

