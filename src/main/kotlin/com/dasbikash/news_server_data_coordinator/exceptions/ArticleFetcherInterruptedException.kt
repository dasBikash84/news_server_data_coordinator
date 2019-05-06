package com.dasbikash.news_server_data_coordinator.exceptions

class ArticleFetcherInterruptedException:MediumLevelException {
    constructor(message: String?) : super(message)
}