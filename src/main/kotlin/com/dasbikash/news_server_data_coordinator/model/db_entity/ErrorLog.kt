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

import javax.persistence.*

@Entity
@Table(name = "exception_log")
class ErrorLog(exception: Throwable){
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int = 0
        @Transient
        val exception:Throwable
        val exceptionClassFullName:String
        val exceptionClassSimpleName:String
        @Column(columnDefinition="text")
        var exceptionCause:String=""
        @Column(columnDefinition="text")
        var exceptionMessage:String=""
        @Column(columnDefinition="text")
        var stackTrace:String =""

        init {
                this.exception=exception
                this.exceptionClassSimpleName = exception::class.java.simpleName
                this.exceptionClassFullName = exception::class.java.canonicalName
                this.exceptionCause = exception.cause?.message ?: ""
                this.exceptionMessage = exception.message ?: ""
//                val stackTrace = mutableListOf<StackTraceElement>()
                val stackTraceBuilder = StringBuilder("")
                exception.stackTrace.asSequence().forEach { stackTraceBuilder.append(it.toString()).append("\n") }
                this.stackTrace = stackTraceBuilder.toString()
        }

        override fun toString(): String {
                return "exceptionClassFullName='$exceptionClassFullName',\n" +
                        "exceptionClassSimpleName='$exceptionClassSimpleName',\n" +
                        "exceptionCause='$exceptionCause',\n" +
                        "exceptionMessage='$exceptionMessage',\n" +
                        "stackTrace='$stackTrace"
        }


}