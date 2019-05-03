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

package com.dasbikash.news_server_data_coordinator.database

import com.dasbikash.news_server_data_coordinator.model.*
import org.hibernate.Session
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils

object DatabaseUtils {

    private val DB_WRITE_MAX_RETRY = 3

    fun runDbTransection(session: Session, operation: () -> Unit): Boolean {

        var retryLimit = DB_WRITE_MAX_RETRY;

        var exception:java.lang.Exception

        do {
            try {
                if (!session.transaction.isActive) {
                    session.beginTransaction()
                }
                operation()
                session.transaction.commit()
                return true
            } catch (ex: Exception) {
                ex.printStackTrace()
                exception = ex
            }
        }while (--retryLimit>0)

        val stackTrace = mutableListOf<StackTraceElement>()
        exception.stackTrace.toCollection(stackTrace)

        try {
            if (!session.transaction.isActive) {
                session.beginTransaction()
            }
            LoggerUtils.logMessage("Message: ${exception.message} Cause: ${exception.cause?.message} StackTrace: ${stackTrace}", session)
            session.transaction.commit()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return false
    }

    fun getLanguageMap(session: Session): Map<String,Language>{
        val hql = "FROM ${EntityClassNames.LANGUAGE}"
        val query = session.createQuery(hql, Language::class.java)
        val languageMapFromDb = mutableMapOf<String,Language>()

        (query.list() as List<Language>).asSequence().forEach {
            languageMapFromDb.put(it.id,it)
        }
        return languageMapFromDb
    }

    fun getCountriesMap(session: Session): Map<String,Country>{
        val hql = "FROM ${EntityClassNames.COUNTRY}"
        val query = session.createQuery(hql, Country::class.java)
        val countriesMap = mutableMapOf<String,Country>()
        (query.list() as List<Country>).asSequence()
                .forEach {
                    countriesMap.put(it.name,it)
                }
        return countriesMap
    }

    fun getNewspaperMap(session: Session): Map<String,Newspaper>{
        val hql = "FROM ${EntityClassNames.NEWSPAPER} where active=true"
        val query = session.createQuery(hql, Newspaper::class.java)
        val newspaperMap = mutableMapOf<String,Newspaper>()
        (query.list() as List<Newspaper>).asSequence()
                .forEach {
                    newspaperMap.put(it.id,it)
                }
        return newspaperMap
    }

    /*fun findArticleById(session: Session,id:String): Article?{
        val hql = "FROM ${EntityClassNames.ARTICLE} where id='${id}'"
        val query = session.createQuery(hql, Article::class.java)
        val resultList = query.list() as List<Article>
        if (resultList.size> 0){
            return resultList.get(0)
        }
        return null
    }*/

    fun findNewspaperById(session: Session,id:String): Newspaper?{
        val hql = "FROM ${EntityClassNames.NEWSPAPER} where id='${id}'"
        val query = session.createQuery(hql, Newspaper::class.java)
        val resultList = query.list() as List<Newspaper>
        if (resultList.size == 1){
            return resultList.get(0)
        }
        return null
    }
}