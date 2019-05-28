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

import java.io.File
import java.util.*

object FileUtils {

    private val DAILY_REPORT_FILE_NAME_PREFIX = "data_coordinator_daily_report_"
    private val WEEKLY_REPORT_FILE_NAME_PREFIX = "data_coordinator_weekly_report_"
    private val MONTHLY_REPORT_FILE_NAME_PREFIX = "data_coordinator_monthly_report_"
    private val REPORT_FILE_NAME_EXT = ".csv"
    private val HOME_DIR_PATH:String
    private val PROJECT_HOME_DIR_PATH:String
    private val PROJECT_DIR_NAME = ".ns-data-coordinator"

    init {
        HOME_DIR_PATH = System.getProperty("user.home")
        PROJECT_HOME_DIR_PATH = HOME_DIR_PATH+"/"+ PROJECT_DIR_NAME+"/"
        if (!File(PROJECT_HOME_DIR_PATH).exists()){
            File(PROJECT_HOME_DIR_PATH).mkdir()
        }
    }

    fun getDailyReportFilePath(today: Date):String{
        val yesterDay = Calendar.getInstance()
        yesterDay.time = today
        yesterDay.add(Calendar.DAY_OF_YEAR,-1)
        return StringBuilder(PROJECT_HOME_DIR_PATH).append(DAILY_REPORT_FILE_NAME_PREFIX)
                .append(DateUtils.getDateStringForDb(yesterDay.time)).append(REPORT_FILE_NAME_EXT)
                .toString()
    }

    fun getWeeklyReportFilePath(thisWeekFirstDay: Date):String{
        val lastWeekFirstDay = Calendar.getInstance()
        lastWeekFirstDay.time = thisWeekFirstDay
        lastWeekFirstDay.add(Calendar.DAY_OF_YEAR,-7)

        val lastWeekLastDay = Calendar.getInstance()
        lastWeekLastDay.time = thisWeekFirstDay
        lastWeekLastDay.add(Calendar.DAY_OF_YEAR,-1)

        return StringBuilder(PROJECT_HOME_DIR_PATH).append(WEEKLY_REPORT_FILE_NAME_PREFIX)
                .append(DateUtils.getDateStringForDb(lastWeekFirstDay.time))
                .append("_to_")
                .append(DateUtils.getDateStringForDb(lastWeekLastDay.time))
                .append(REPORT_FILE_NAME_EXT)
                .toString()
    }

    fun getMonthlyReportFilePath(thisMonthFirstDay: Date):String{
        val lastMonthFirstDay = DateUtils.getFirstDayOfLastMonth(thisMonthFirstDay)

        return StringBuilder(PROJECT_HOME_DIR_PATH).append(MONTHLY_REPORT_FILE_NAME_PREFIX)
                .append(DateUtils.getYearMonthStr(lastMonthFirstDay))
                .append(REPORT_FILE_NAME_EXT)
                .toString()
    }
}