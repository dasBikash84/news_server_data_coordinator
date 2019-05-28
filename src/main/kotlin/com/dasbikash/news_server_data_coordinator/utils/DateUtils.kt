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

package com.dasbikash.news_server_data_coordinator.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DB_DATE_STRING_FORMAT = "YYYY-MM-dd"
    private const val MONTH_YEAR_STRING_FORMAT = "MMM-YYYY"
    private val simpleDateFormatForDb = SimpleDateFormat(DB_DATE_STRING_FORMAT)
    private val simpleDateFormatForYearMonthStr = SimpleDateFormat(MONTH_YEAR_STRING_FORMAT)

    fun getDateStringForDb(date: Date):String = simpleDateFormatForDb.format(date)
    fun getYearMonthStr(date: Date):String = simpleDateFormatForYearMonthStr.format(date)

    fun getFirstDayOfMonth(anyDayOfMonth: Date):Date{
        val firstDayOfMonth = Calendar.getInstance()
        firstDayOfMonth.time = anyDayOfMonth
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH,1)
        return firstDayOfMonth.time
    }

    fun getFirstDayOfLastMonth(anyDayOfMonth: Date):Date{
        val firstDayOfNextMonth = Calendar.getInstance()
        firstDayOfNextMonth.time = anyDayOfMonth
        firstDayOfNextMonth.set(Calendar.DAY_OF_MONTH,1)
        firstDayOfNextMonth.add(Calendar.DAY_OF_YEAR,-1)
        firstDayOfNextMonth.set(Calendar.DAY_OF_MONTH,1)
        return firstDayOfNextMonth.time
    }

    fun isFirstDayOfWeek(date: Date):Boolean{
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
    }

    fun isFirstDayOfMonth(date: Date):Boolean{
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.DAY_OF_MONTH) == 1
    }

    fun getYesterDay(today:Date):Date{
        val yesterDay = Calendar.getInstance()
        yesterDay.time = today
        yesterDay.add(Calendar.DAY_OF_YEAR, -1)
        return yesterDay.time
    }

    fun getLastWeekSameDay(today:Date):Date{
        val lastWeekSameDay = Calendar.getInstance()
        lastWeekSameDay.time = today
        lastWeekSameDay.add(Calendar.DAY_OF_YEAR, -7)
        return lastWeekSameDay.time
    }
}