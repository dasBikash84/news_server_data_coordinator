package com.dasbikash.news_server_data_coordinator.exceptions

import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget

class SettingsUploadException:HighestLevelException {
    constructor(articleUploadTarget: ArticleUploadTarget,throwable: Throwable)
            :super("SettingsUploadException for ${articleUploadTarget}",throwable)
}