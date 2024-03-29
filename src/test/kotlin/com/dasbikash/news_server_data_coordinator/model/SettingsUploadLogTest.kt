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

package com.dasbikash.news_server_data_coordinator.model

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class SettingsUploadLogTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }


//    @Test
//    fun testDataIO(){
//        val session = DbSessionManager.getNewSession()

        /*for (i in 1..5){
            DatabaseUtils.runDbTransection(session){
                session.save(SettingsUploadLog(uploadTarget = ArticleUploadTarget.REAL_TIME_DB))
            }
            Thread.sleep(5000)
        }*/

//        val settingsUpdateLog = DatabaseUtils.getLastSettingsUpdateLog(session)
//        val settingsUploadLog = DatabaseUtils.getLastSettingsUploadLogByTarget(session, ArticleUploadTarget.REAL_TIME_DB)
//        if (settingsUploadLog !=null){
//            if (settingsUploadLog.uploadTime < settingsUpdateLog.updateTime){
//                println("Have to update settings")
//            }
//        }else{
//            println("Have to update settings")
//        }
//        session.close()
//    }
}