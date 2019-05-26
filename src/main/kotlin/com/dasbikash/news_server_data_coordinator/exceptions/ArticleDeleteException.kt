package com.dasbikash.news_server_data_coordinator.exceptions

import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget

class ArticleDeleteException(articleUploadTarget: ArticleUploadTarget, throwable: Throwable)
    : HighestLevelException("SettingsUploadException for ${articleUploadTarget}", throwable) {
}