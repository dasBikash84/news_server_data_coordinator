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

import com.google.firebase.database.DatabaseReference

object FirebaseDbRefUtils {

    private const val APP_SETTINGS_NODE = "app_settings"
//    private const val USER_SETTINGS_ROOT_NODE = "user_settings"
    private const val ARTICLE_DATA_ROOT_NODE = "article_data"
    private const val UPDATE_LOG_NODE = "update_log"

    private const val COUNTRIES_NODE = "countries"
    private const val LANGUAGES_NODE = "languages"
    private const val NEWSPAPERS_NODE = "newspapers"
    private const val PAGES_NODE = "pages"
    private const val PAGE_GROUPS_NODE = "page_groups"
    private const val APP_SETTINGS_UPDATE_TIME_NODE = "update_time"

    private lateinit var mRootReference:DatabaseReference
    private lateinit var  mAppSettingsRootReference: DatabaseReference
//    private lateinit var  mUserSettingsRootReference: DatabaseReference
    private lateinit var  mArticleDataRootReference: DatabaseReference

    internal fun getRootRef():DatabaseReference{
        if (!::mRootReference.isInitialized){
            mRootReference = FireBaseConUtils.mFirebaseDatabaseCon.reference
        }
        return mRootReference
    }
    internal fun getAppSettingsRootRef():DatabaseReference{
        if (!::mAppSettingsRootReference.isInitialized){
            mAppSettingsRootReference = getRootRef().child(APP_SETTINGS_NODE)
        }
        return mAppSettingsRootReference
    }

//    internal fun getUserSettingsRootReference():DatabaseReference{
//        if (!::mUserSettingsRootReference.isInitialized){
//            mUserSettingsRootReference = getRootRef().child(USER_SETTINGS_ROOT_NODE)
//        }
//        return mUserSettingsRootReference
//    }

    internal fun getArticleDataRootReference():DatabaseReference{
        if (!::mArticleDataRootReference.isInitialized){
            mArticleDataRootReference = getRootRef().child(ARTICLE_DATA_ROOT_NODE)
        }
        return mArticleDataRootReference
    }

    internal fun getUpdateLogRef():DatabaseReference = getRootRef().child(UPDATE_LOG_NODE)

    internal fun getCountriesRef():DatabaseReference = getAppSettingsRootRef().child(COUNTRIES_NODE)
    internal fun getLanguagesRef():DatabaseReference = getAppSettingsRootRef().child(LANGUAGES_NODE)
    internal fun getNewspapersRef():DatabaseReference = getAppSettingsRootRef().child(NEWSPAPERS_NODE)
    internal fun getPagesRef():DatabaseReference = getAppSettingsRootRef().child(PAGES_NODE)
    internal fun getPageGroupsRef():DatabaseReference = getAppSettingsRootRef().child(PAGE_GROUPS_NODE)
    internal fun getSettingsUpdateTimeRef():DatabaseReference = getAppSettingsRootRef().child(APP_SETTINGS_UPDATE_TIME_NODE)

}