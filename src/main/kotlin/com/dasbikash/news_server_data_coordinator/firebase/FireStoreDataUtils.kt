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
import java.lang.IllegalArgumentException


object FireStoreDataUtils {

    private val ARTICLE_COLLECTION_LABEL = "articles"
    private val MAX_BATCH_DOCUMENT_WRITE_COUNT = 400

    private val mArticleCollectionReference = FireBaseConUtils.mFireStoreCon.collection(ARTICLE_COLLECTION_LABEL)

    fun writeArticleData(articles:List<Article>){

        if (articles.size> MAX_BATCH_DOCUMENT_WRITE_COUNT){
            throw IllegalArgumentException()
        }

        val batch = FireBaseConUtils.mFireStoreCon.batch()

        articles.asSequence().forEach {
            batch.set(mArticleCollectionReference.document(), ArticleForFB.fromArticle(it))
        }
        val future = batch.commit()
        while (!future.isDone){}
    }

}
