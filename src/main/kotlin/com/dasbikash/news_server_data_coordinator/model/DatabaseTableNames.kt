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
    const val PAGE_GROUP_TABLE_NAME = "page_groups";
}