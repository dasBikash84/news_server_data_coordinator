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

import com.dasbikash.news_server_data_coordinator.model.Article
import com.google.api.core.ApiFuture
import com.google.firebase.database.DatabaseReference


object RealTimeDbDataUtils {

    private val mArticleDataRootReference:DatabaseReference = FirebaseDbRefUtils.getArticleDataRootReference()

    fun writeArticleData(articleList:List<Article>){
        val futureList = mutableListOf<ApiFuture<Void>>()

        articleList.asSequence().forEach {
            futureList.add(mArticleDataRootReference.child(it.page!!.id).push().setValueAsync(ArticleForFB.fromArticle(it)))
        }
        futureList.asSequence().forEach {
            while (!it.isDone){}
        }
    }

    fun clearAllArticleData(){
//        val task = mArticleDataRootReference.setValueAsync(null)
//        while (!task.isDone){
//            println("Waiting for data deletion.")
//            Thread.sleep(1000L)
//        }
    }


}
