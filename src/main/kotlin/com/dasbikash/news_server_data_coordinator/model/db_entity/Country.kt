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
import com.google.cloud.firestore.annotation.Exclude
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.COUNTRY_TABLE_NAME)
data class Country (
        @Id var name: String="",
        var countryCode: String?=null,
        var timeZone: String?=null
){
        fun updateData(newCountry: Country) {
                this.countryCode = newCountry.countryCode
                this.timeZone = newCountry.timeZone
                updated = true
        }

        @OneToMany(targetEntity = Newspaper::class,mappedBy = "country",fetch = FetchType.LAZY)
        @Exclude
        @com.google.firebase.database.Exclude
        var newsPapers:List<Newspaper>? = null
        var updated:Boolean = true


}