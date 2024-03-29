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

package com.dasbikash.news_server_data_coordinator.model.db_entity

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class ArticleImage(
        @Column(columnDefinition = "text")
        var link: String? = null,
        @Column(columnDefinition = "text")
        var caption: String? = null
) {
    fun getDataAsMap(): Map<String, String?> {
        val dataMap = mutableMapOf<String, String?>()
        dataMap.put("link", link)
        dataMap.put("caption", caption)
        return dataMap.toMap()
    }
}