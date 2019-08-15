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

package com.dasbikash.news_server_data_coordinator.model

object DatabaseTableNames {
    const val COUNTRY_TABLE_NAME = "countries";
    const val LANGUAGE_TABLE_NAME = "languages";
    const val NEWSPAPER_TABLE_NAME = "newspapers";
    const val PAGE_TABLE_NAME = "pages";
    const val ARTICLE_TABLE_NAME = "articles";
    const val SETTINGS_UPDATE_LOG_TABLE_NAME = "settings_update_log";
    const val SETTINGS_UPLOAD_LOG_TABLE_NAME = "settings_upload_log";
    const val ARTICLE_UPLOADER_STATUS_CHANGE_LOG_TABLE_NAME = "article_uploader_status_change_log";
    const val ARTICLE_DOWNLOAD_LOG_TABLE_NAME = "article_download_log";
    const val ARTICLE_UPLOAD_LOG_TABLE_NAME = "article_upload_log";
    const val PAGE_GROUP_TABLE_NAME = "page_groups";
    const val ARTICLE_DELETE_REQUEST_TABLE_NAME = "article_delete_request"
    const val AUTH_TOKEN_TABLE_NAME = "tokens"
    const val DAILY_DELETION_TASK_LOG_TABLE_NAME = "daily_deletion_task_log"
    const val RESTRICTED_SEARCH_KEY_WORD_TABLE_NAME = "restricted_search_key_word"
    const val KEY_WORD_SERACH_RESULT_TABLE_NAME = "key_word_serach_result"
    const val ARTICLE_SEARCH_RESULT_UPLOADER_LOG_TABLE_NAME = "article_search_result_uploader_log"
    const val ARTICLE_SEARCH_RESULT_DELETION_LOG_TABLE_NAME = "article_search_result_deletion_log"
    const val NEWS_CATERORIES_ENTRY_NAME = "news_categories"
    const val NEWS_CATEGORY_ENTRY_ENTRY_NAME = "news_category_entry"
    const val FIREBASE_USER_ENTRY_NAME = "firebase_user"
    const val FIREBASE_USER_INFO_SYNCHRONIZER_LOG_ENTRY_NAME = "firebase_user_info_synchronizer_log"
    const val FAV_PAGE_ENTRY_ON_USER_SETTINGS_TABLE_NAME = "fav_page_entry_on_user_settings"
}