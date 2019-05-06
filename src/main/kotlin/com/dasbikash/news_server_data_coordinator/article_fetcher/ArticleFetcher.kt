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

package com.dasbikash.news_server_data_coordinator.article_fetcher

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.exceptions.ArticleFetcherInterruptedException
import com.dasbikash.news_server_data_coordinator.exceptions.DataCoordinatorException
import com.dasbikash.news_server_data_coordinator.exceptions.handlers.DataCoordinatorExceptionHandler
import com.dasbikash.news_server_data_coordinator.model.db_entity.Newspaper
import com.dasbikash.news_server_data_coordinator.settings_loader.DataFetcherFromParser
import kotlin.random.Random

class ArticleFetcher(val newspaper: Newspaper)
    : Thread() {

    private val WAITING_TIME_BETWEEN_PAGES = 1000L

    private val INIT_DELAY_FOR_ERROR = 60 * 1000L
    private var errorDelayPeriod = 0L
    private var errorIteration = 0L

    override fun run() {
        super.run()

        println("ArticleFetcher for ${newspaper.name} started")

        do {
            val session = DbSessionManager.getNewSession()
            val newspaperForTask = DatabaseUtils.findNewspaperById(session, newspaper.id)!!
            newspaperForTask.pageList.asSequence().forEach {
                try {
                    val currentPage = it
                    var fetchedArticles = DataFetcherFromParser.getLatestArticlesForPage(currentPage)
                    while (fetchedArticles.size > 0) {
                        var savedArticleCount = 0
                        fetchedArticles.asSequence().forEach {
                            if (DatabaseUtils.findArticleById(session, it.id) == null) {
                                DatabaseUtils.runDbTransection(session) {
                                    session.save(it)
                                    savedArticleCount++
                                }
                            }
                        }
                        if (savedArticleCount > 0) {
                            println("${savedArticleCount} articles saved for page: ${currentPage.name} Np: ${newspaper.name}")
                        }
                        sleep(100L)
                        if (savedArticleCount < fetchedArticles.size) {
                            sleep(100L)
                            break
                        }
                        fetchedArticles = DataFetcherFromParser.getArticlesBeforeGivenArticleForPage(currentPage, fetchedArticles.last())
                    }
                    sleep(100L)

                    val oldestArticle = DatabaseUtils.findOldestArticleForPage(session, currentPage)
                    oldestArticle?.let {
                        fetchedArticles = DataFetcherFromParser.getArticlesBeforeGivenArticleForPage(currentPage, it)
//                        println("Found ${fetchedArticles.size} articles after oldest ${it} for : Page: ${currentPage.name} Np: ${newspaper.name}")
                        while (fetchedArticles.size > 0) {
                            var savedArticleCount = 0
                            fetchedArticles.asSequence().forEach {
                                if (DatabaseUtils.findArticleById(session, it.id) == null) {
                                    DatabaseUtils.runDbTransection(session) {
                                        session.save(it)
                                        savedArticleCount++
                                    }
                                }
                            }
                            if (savedArticleCount > 0) {
                                println("${savedArticleCount} articles saved for page: ${currentPage.name} Np: ${newspaper.name}")
                            }
                            sleep(100L)
                            fetchedArticles = DataFetcherFromParser.getArticlesBeforeGivenArticleForPage(currentPage, fetchedArticles.last())
                        }

                    }
                    sleep(WAITING_TIME_BETWEEN_PAGES + Random(System.currentTimeMillis()).nextLong(WAITING_TIME_BETWEEN_PAGES))
                    errorDelayPeriod = 0L
                    errorIteration = 0L

                } catch (ex: InterruptedException) {
                    ex.printStackTrace()
                    DataCoordinatorExceptionHandler
                            .handleException(
                                    ArticleFetcherInterruptedException("ArticleFetcher for ${newspaperForTask.name} Interrupted")
                            )
                    return
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    DataCoordinatorExceptionHandler
                            .handleException(
                                    DataCoordinatorException(ex)
                            )
                    try {
                        sleep(getErrorDelayPeriod())
                    } catch (ex: InterruptedException) {
                        ex.printStackTrace()
                    }
                }
            }
            session.close()
        } while (true)
    }

    private fun getErrorDelayPeriod(): Long {
        errorIteration++
        errorDelayPeriod += (INIT_DELAY_FOR_ERROR * errorIteration)
        return errorDelayPeriod
    }


}
