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

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.SETTINGS_UPLOAD_LOG_TABLE_NAME)
class SettingsUploadLog (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id:Int?=null,
        var uploadTime:Date = Date(),
        @Column(columnDefinition = "enum('REAL_TIME_DB','FIRE_STORE_DB','MONGO_REST_SERVICE')")
        @Enumerated(EnumType.STRING)
        var uploadTarget: ArticleUploadTarget?=null
){
        override fun toString(): String {
                return "SettingsUploadLog(uploadTime=$uploadTime, uploadTarget=$uploadTarget)"
        }
}