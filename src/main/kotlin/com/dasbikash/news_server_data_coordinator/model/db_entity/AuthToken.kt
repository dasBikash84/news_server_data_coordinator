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
import com.google.cloud.Timestamp
import com.google.firebase.database.Exclude
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Transient

@Entity
@Table(name = DatabaseTableNames.AUTH_TOKEN_TABLE_NAME)
class AuthToken(){
    @Id
    val token:String = UUID.randomUUID().toString()
    var expiresOn:Date

    @Transient
    fun hasExpired():Boolean{
        return System.currentTimeMillis() > expiresOn.time
    }

    fun makeExpired(){
        expiresOn = Date()
    }

    override fun toString(): String {
        return "AuthToken(token='$token', expiresOn=$expiresOn hasExpired:${hasExpired()})"
    }

    init{
        val calander = Calendar.getInstance()
        calander.add(Calendar.MINUTE,TOKEN_LIFE_TIME_MINUTES)
        expiresOn = calander.time
    }
    companion object{
        private const val TOKEN_LIFE_TIME_MINUTES = 5
    }

}

data class TokenGenerationRequest(var timeStamp: TimeStamp? = null){

    @Exclude
    fun isValid():Boolean{
        if (timeStamp == null) return false
        val calendar = Calendar.getInstance()
        calendar.time = Timestamp.ofTimeSecondsAndNanos(timeStamp!!.seconds,timeStamp!!.nanoseconds).toDate()
        return (System.currentTimeMillis() - calendar.timeInMillis) < MAX_ALLOWED_AGE_MS
    }

    companion object{
        private const val MAX_ALLOWED_AGE_MINUTE = 5
        private const val MAX_ALLOWED_AGE_MS = MAX_ALLOWED_AGE_MINUTE*60*1000L
    }
}

data class TimeStamp(var seconds:Long=0,var nanoseconds:Int=0)