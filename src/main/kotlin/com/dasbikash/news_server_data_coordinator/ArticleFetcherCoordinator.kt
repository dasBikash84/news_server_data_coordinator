@file:JvmName("com.dasbikash.news_server_data_coordinator.ArticleFetcherCoordinator")

package com.dasbikash.news_server_data_coordinator

import com.dasbikash.news_server_data_coordinator.article_data_uploader.ArticleDataUploader
import com.dasbikash.news_server_data_coordinator.article_data_uploader.ArticleDataUploaderForRealTimeDb
import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.SettingsUpdateLog
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

    private val ARTICLE_FETCHER_MAP: MutableMap<String, ArticleFetcher> = mutableMapOf()
    private val SETTINGS_UPDATE_ITERATION_PERIOD = 10 * 60 * 60 * 1000L
    private lateinit var articleDataUploader: ArticleDataUploader

    @JvmStatic
    fun main(args: Array<String>) {
        do {
            val session = DbSessionManager.getNewSession()
            var settingsUpdated = false
            val settingsUpdateLog = StringBuilder()

            fun setSettingsUpdated() { settingsUpdated=true}

            //For language settings current support is for adding new languages and editing current ones
            //Read current language settings from parser
            val languageMapFromParser = DataFetcherFromParser.getLanguageMap()

            //Read current language settings from own DB
            val languageMapFromDb = DatabaseUtils.getLanguageMap(session)

            val newLanguageIds = ArrayList(languageMapFromParser.keys)
            newLanguageIds.removeAll(languageMapFromDb.keys)

            if(newLanguageIds.isNotEmpty()){
                setSettingsUpdated()
            }

            newLanguageIds.asSequence().forEach {
                settingsUpdateLog.append("Language added id: ${it} | ")
                DatabaseUtils.runDbTransection(session) {
                    session.save(languageMapFromParser.get(it))
                }
            }
            languageMapFromDb.keys.asSequence()
                    .forEach {
                        if (languageMapFromParser.containsKey(it) &&
                                !languageMapFromParser.get(it)!!.equals(languageMapFromDb.get(it))) {
                            setSettingsUpdated()
                            settingsUpdateLog.append("Language modified id: ${it} | ")
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
            val countriesMapFromDb = DatabaseUtils.getCountriesMap(session)

            val newCountryIds = ArrayList(countriesMapFromParser.keys)
            newCountryIds.removeAll(countriesMapFromDb.keys)

            if(newCountryIds.isNotEmpty()){
                setSettingsUpdated()
            }

            newCountryIds.asSequence().forEach {
                settingsUpdateLog.append("Country added id: ${it} | ")
                DatabaseUtils.runDbTransection(session) {
                    session.save(countriesMapFromParser.get(it))
                }
            }

            countriesMapFromDb.keys.asSequence()
                    .forEach {
                        if (countriesMapFromParser.containsKey(it) &&
                                !countriesMapFromParser.get(it)!!.equals(countriesMapFromDb.get(it))) {
                            setSettingsUpdated()
                            settingsUpdateLog.append("Country modified id: ${it} | ")
                            val oldCountry = countriesMapFromDb.get(it)
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

            if(newNewspaperIds.isNotEmpty()){
                setSettingsUpdated()
            }
            newNewspaperIds.asSequence().forEach {
                val newspaperFromDb = DatabaseUtils.findNewspaperById(session, it)

                if (newspaperFromDb == null) {
                    settingsUpdateLog.append("Newspaper added id: ${it} | ")
                    val newNewspaper = newsPaperMapFromParser.get(it)!!
                    println("new Newspapers found : ${newNewspaper}")
                    val pages = DataFetcherFromParser.getPagesForNewspaper(newNewspaper)
                    newNewspaper.pageList = pages.toMutableList()
                    DatabaseUtils.runDbTransection(session) {
                        session.save(newNewspaper)
                        newNewspaper.pageList.forEach {
                            settingsUpdateLog.append("Page added id: ${it.id} | ")
                            session.save(it)
                        }
                    }
                } else {
                    settingsUpdateLog.append("Newspaper activated id: ${it} | ")
                    newspaperFromDb.active = true
                    newspaperFromDb.pageList.asSequence().forEach { it.active = false }
                    val pagesFromParser = DataFetcherFromParser.getPagesForNewspaper(newspaperFromDb)
                    pagesFromParser.asSequence().forEach {
                        if (newspaperFromDb.pageList.contains(it)) {
                            val pageFromDb = newspaperFromDb.pageList.get(newspaperFromDb.pageList.indexOf(it))
                            pageFromDb.getContentFromOther(it)
//                            pageFromDb.hasChild = it.hasChild
                        } else {
                            DatabaseUtils.runDbTransection(session) {
                                settingsUpdateLog.append("Page added id: ${it.id} | ")
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

                val articleFetcher = ArticleFetcher(newNewspaper, newNewspaper.pageList)
                ARTICLE_FETCHER_MAP.put(newNewspaper.id, articleFetcher)
                articleFetcher.start()
            }

            deactivatedIds.asSequence()
                    .forEach {
                        setSettingsUpdated()
                        settingsUpdateLog.append("Newspaper deactivated id: ${it} | ")
                        val deactivatedNewspaper = newsPaperMapFromDb.get(it)!!
                        deactivatedNewspaper.active = false
                        DatabaseUtils.runDbTransection(session) {
                            session.update(deactivatedNewspaper)
                        }
                        if (ARTICLE_FETCHER_MAP.containsKey(it)) {
                            ARTICLE_FETCHER_MAP.get(it)!!.interrupt()
                            ARTICLE_FETCHER_MAP.remove(it)
                        }
                    }

            val unChangedNewspaperIds = ArrayList(newsPaperMapFromParser.keys)
            unChangedNewspaperIds.removeAll(newNewspaperIds)

            unChangedNewspaperIds.asSequence().forEach {
                val unChangedNewspaper = newsPaperMapFromParser.get(it)!!
                val pagesFromParser = DataFetcherFromParser.getPagesForNewspaper(unChangedNewspaper)
                val unChangedNewspaperFromDb = newsPaperMapFromDb.get(it)!!
                if (pagesFromParser.size > unChangedNewspaperFromDb.pageList.size) {
                    setSettingsUpdated()
                    if (ARTICLE_FETCHER_MAP.containsKey(it)) {
                        ARTICLE_FETCHER_MAP.get(it)!!.interrupt()
                        ARTICLE_FETCHER_MAP.remove(it)
                    }
                    pagesFromParser.asSequence().forEach {
                        if (!unChangedNewspaperFromDb.pageList.contains(it)) {
                            DatabaseUtils.runDbTransection(session) {
                                settingsUpdateLog.append("Page added id: ${it.id} | ")
                                session.save(it)
                            }
                            unChangedNewspaperFromDb.pageList.add(it)
                        }
                    }
                    val articleFetcher = ArticleFetcher(unChangedNewspaperFromDb, unChangedNewspaperFromDb.pageList)
                    ARTICLE_FETCHER_MAP.put(unChangedNewspaperFromDb.id, articleFetcher)
                    articleFetcher.start()
                } else {
                    if (ARTICLE_FETCHER_MAP.get(it)==null || !ARTICLE_FETCHER_MAP.get(it)!!.isAlive) {
                        ARTICLE_FETCHER_MAP.remove(it)
                        val unChangedNewspaperFromDb = newsPaperMapFromDb.get(it)!!
                        val articleFetcher = ArticleFetcher(unChangedNewspaperFromDb, unChangedNewspaperFromDb.pageList)
                        ARTICLE_FETCHER_MAP.put(it, articleFetcher)
                        articleFetcher.start()
                    }
                }
            }
            if (settingsUpdated){
                DatabaseUtils.runDbTransection(session){
                    session.save(SettingsUpdateLog(logMessage = settingsUpdateLog.toString()))
                }
            }
            session.close()

            if(!::articleDataUploader.isInitialized){
                articleDataUploader = ArticleDataUploaderForRealTimeDb()
                articleDataUploader.start()
            }else{
                if (!articleDataUploader.isAlive){
                    articleDataUploader = ArticleDataUploaderForRealTimeDb()
                    articleDataUploader.start()
                }
            }

            try {
                Thread.sleep(SETTINGS_UPDATE_ITERATION_PERIOD)
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
            }

        } while (true)
    }
}