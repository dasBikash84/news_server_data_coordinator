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

object EntityClassNames {
    const val COUNTRY = "Country"
    const val LANGUAGE = "Language"
    const val NEWSPAPER = "Newspaper"
    const val PAGE = "Page"
    const val ARTICLE = "Article"
    const val PAGE_GROUP = "PageGroup"
    const val SETTINGS_UPLOAD_LOG = "SettingsUploadLog"
    const val SETTINGS_UPDATE_LOG = "SettingsUpdateLog"
    const val ARTICLE_UPLOADER_STATUS_CHANGE_LOG = "ArticleUploaderStatusChangeLog"
    const val ARTICLE_DOWNLOAD_LOG = "ArticleDownloadLog"
    const val ARTICLE_DELETE_REQUEST = "ArticleDeleteRequest"
    const val DAILY_DELETION_TASK_LOG = "DailyDeletionTaskLog"
    const val RESTRICTED_SEARCH_KEY_WORD = "RestrictedSearchKeyWord"
    const val KEY_WORD_SERACH_RESULT = "KeyWordSearchResult"
    const val NEWS_CATERORIES = "NewsCategory"
    const val NEWS_CATERORY_ENTRIES = "NewsCategoryEntry"
}