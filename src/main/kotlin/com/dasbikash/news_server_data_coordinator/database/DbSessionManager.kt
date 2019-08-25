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

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import java.util.logging.Level
import java.util.logging.LogManager

object DbSessionManager {

//    val CONFIG_FILE_PATH = "src/main/resources/hibernate.cfg.xml";

    val configuration:Configuration
    val sessionFactory: SessionFactory

    init {
        disableLogging()
        configuration = Configuration().configure(/*File(CONFIG_FILE_PATH)*/)
        sessionFactory = configuration.buildSessionFactory()
    }

    fun getNewSession():Session{
        return sessionFactory.openSession()
    }

    private fun disableLogging() {
        val logManager = LogManager.getLogManager()
        logManager.getLogger("").level = Level.SEVERE
    }
}