package com.dasbikash.news_server_data_coordinator.exceptions

import com.dasbikash.news_server_data_coordinator.model.db_entity.ArticleUploadTarget

class NotificationGenerationException: HighestLevelException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}