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

package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.google.api.core.ApiFuture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class RealTimeDbDataUtilsTest {

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    //    @Test
//    fun clearAllArticleData(){
//        RealTimeDbDataUtils.clearAllArticleData()
//    }
//    @Test
//    fun searchKeyWordsUploadTest() {
//        val session = DbSessionManager.getNewSession()
//        val futureList = mutableListOf<ApiFuture<Void>>()
//        DatabaseUtils.getSearchKeyWords(session).asSequence().forEach {
//            futureList.add(RealTimeDbRefUtils.getSearchKeyWordsNode()
//                    .child(it).setValueAsync(true))
//        }
//        futureList.asSequence().forEach {
//            while (!it.isDone){}
//        }
//    }
}