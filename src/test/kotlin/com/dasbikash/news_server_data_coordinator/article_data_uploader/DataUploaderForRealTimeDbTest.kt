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

package com.dasbikash.news_server_data_coordinator.article_data_uploader

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import org.hibernate.Session
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class DataUploaderForRealTimeDbTest {
    lateinit var session:Session
    lateinit var dataUploader: DataUploader

    @BeforeEach
    fun setUp() {
        session=DbSessionManager.getNewSession()
        dataUploader = DataUploaderForRealTimeDb()
    }

    @AfterEach
    fun tearDown() {
    }

//    @Test
//    fun testWrite(){
//        val writeThread = DataUploaderForRealTimeDb()
//        writeThread.start()
//        writeThread.join()
        /*val task = RealTimeDbRefUtils.getAppSettingsRootRef().setValueAsync(null)
        while (!task.isDone){}*/
//    }

//    @Test
//    fun getUploadedArticleCountForPage(){
//        DatabaseUtils.getAllPages(session).shuffled().take(10).asSequence().forEach {
//            println()
//            LoggerUtils.logOnConsole(it.toString())
//            LoggerUtils.logOnConsole("getUploadedArticleCountForPage: ${dataUploader.getUploadedArticleCountForPage(session,it)}")
//            println()
//        }
//    }

}