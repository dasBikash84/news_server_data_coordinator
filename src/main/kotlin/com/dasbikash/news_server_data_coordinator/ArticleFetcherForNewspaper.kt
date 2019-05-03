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

package com.dasbikash.news_server_data_coordinator

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.Newspaper
import com.dasbikash.news_server_data_coordinator.model.Page
import com.dasbikash.news_server_data_coordinator.settings_loader.DataFetcherFromParser
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import kotlin.random.Random

class ArticleFetcherForNewspaper(val newspaper: Newspaper, val pages:List<Page>)
    : Thread() {
    val WAITING_TIME_BETWEEN_PAGES=1000L
    override fun run() {
        super.run()
        do {
            pages.asSequence().forEach {
//                println("Starting article fetch for page: ${it.name} Np: ${newspaper.name}")
//                sleep(1000L)
                val session = DbSessionManager.getNewSession()
                try {
                    val currentPage = it
                    val latestArticle = DatabaseUtils.findLatestArticleForPage(session, currentPage)
                    var fetchedArticles = DataFetcherFromParser.getLatestArticlesForPage(currentPage)
//                    println("Fetched ${fetchedArticles.size} latest articles for page: ${currentPage.name} Np: ${newspaper.name}")
//                sleep(1000L)
                    while (fetchedArticles.size > 0) {
                        var savedArticleCount = 0
                        fetchedArticles.asSequence().forEach {
                            if (DatabaseUtils.findArticleById(session, it.id) == null) {
//                                println("New article with id: ${it.id} saved for page: ${currentPage.name} Np: ${newspaper.name}")
//                            sleep(1000L)
                                DatabaseUtils.runDbTransection(session) {
                                    session.save(it)
                                    savedArticleCount++
                                }
                            }
                        }
                        if (savedArticleCount>0) {
                            println("${savedArticleCount} articles saved for page: ${currentPage.name} Np: ${newspaper.name}")
                        }
                        sleep(100L)
                        if (savedArticleCount < fetchedArticles.size) {
//                            println("no new articles for page: ${currentPage.name} Np: ${newspaper.name}")
                            sleep(100L)
                            break
                        }
                        fetchedArticles = DataFetcherFromParser.getArticlesBeforeGivenArticleForPage(currentPage, fetchedArticles.last())
                    }
//                    println("Articles synced for page: ${currentPage.name} Np: ${newspaper.name}")
                    sleep(100L)

                    val lastArticle = DatabaseUtils.findLatestArticleForPage(session,currentPage)
                    lastArticle?.let {
                        fetchedArticles = DataFetcherFromParser.getArticlesBeforeGivenArticleForPage(currentPage, it)
                        while (fetchedArticles.size > 0) {
                            var savedArticleCount = 0
                            fetchedArticles.asSequence().forEach {
                                if (DatabaseUtils.findArticleById(session, it.id) == null) {
//                                    println("New article with id: ${it.id} saved for page: ${currentPage.name} Np: ${newspaper.name}")
//                            sleep(1000L)
                                    DatabaseUtils.runDbTransection(session) {
                                        session.save(it)
                                        savedArticleCount++
                                    }
                                }
                            }
                            if (savedArticleCount>0) {
                                println("${savedArticleCount} articles saved for page: ${currentPage.name} Np: ${newspaper.name}")
                            }
                            sleep(100L)
                            fetchedArticles = DataFetcherFromParser.getArticlesBeforeGivenArticleForPage(currentPage, fetchedArticles.last())
                        }

                    }

                    if (isInterrupted) {
                        return
                    }
                    sleep(WAITING_TIME_BETWEEN_PAGES+ Random(System.currentTimeMillis()).nextLong(WAITING_TIME_BETWEEN_PAGES))
                }catch (ex:Exception){
                    //ex.printStackTrace()
                    LoggerUtils.logError(ex,session)
                }finally {
                    session.close()
                }
            }
        }while (true)
    }


}
