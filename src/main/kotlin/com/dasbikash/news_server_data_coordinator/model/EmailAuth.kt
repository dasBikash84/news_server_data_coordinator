package com.dasbikash.news_server_data_coordinator.model

data class EmailAuth (
    var userName:String? = null,
    var passWord:String? = null,
    var toAddresses:String? = null,
    var properties:Map<String,String>?=null
)