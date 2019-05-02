package com.dasbikash.news_server_data_coordinator.model

enum class ArticleUploadTarget(val targetName:String) {
    FIREBASE_DATABASE("real_time_db"),
    FIRE_STORE_DATABASE("fire_store_db"),
    MONGO_REST("mongo_rest")
}