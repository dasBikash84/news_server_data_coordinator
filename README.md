# Data Coordinator for News-Server project
Console app (written in Kotlin) to synchronize settings and article data between [`parser`](https://github.com/dasBikash84/ns_reloaded_backend_Parser_) 
app and app data sources. It downloads settings and article data from the parser app via parser's [`rest service`](https://github.com/dasBikash84/news_server_parser_rest_end_point)
and uploads that data to app data sources.

#### Key classes of this repo are:
* [`DataCoordinator`](https://github.com/dasBikash84/news_server_data_coordinator/blob/master/src/main/kotlin/com/dasbikash/news_server_data_coordinator/DataCoordinator.kt)
: It is the entry point and coordinator of all other classes of the application. It runs an infinite loop for continuous operation of designated app functions.
* [`DataFetcherFromParser`](https://github.com/dasBikash84/news_server_data_coordinator/blob/master/src/main/kotlin/com/dasbikash/news_server_data_coordinator/settings_loader/DataFetcherFromParser.kt)
: Singleton class to read data from parser rest service.
* [`ArticleFetcher`](https://github.com/dasBikash84/news_server_data_coordinator/blob/master/src/main/kotlin/com/dasbikash/news_server_data_coordinator/article_fetcher/ArticleFetcher.kt)
: Sub class of Thread defined to download and store article data for any single NewsPaper.
* [`DataUploader`](https://github.com/dasBikash84/news_server_data_coordinator/blob/master/src/main/kotlin/com/dasbikash/news_server_data_coordinator/article_data_uploader/DataUploader.kt)
: Base abstract class for uploading data to remote data source.
* [`DataUploaderForRealTimeDb`](https://github.com/dasBikash84/news_server_data_coordinator/blob/master/src/main/kotlin/com/dasbikash/news_server_data_coordinator/article_data_uploader/DataUploaderForRealTimeDb.kt)
: *DataUploader* implementation for Firebase Real Time Database.
* [`DataUploaderForFireStoreDb`](https://github.com/dasBikash84/news_server_data_coordinator/blob/master/src/main/kotlin/com/dasbikash/news_server_data_coordinator/article_data_uploader/DataUploaderForFireStoreDb.kt)
: *DataUploader* implementation for Firebase FireStore Database.
* [`DataUploaderForMongoRestService`](https://github.com/dasBikash84/news_server_data_coordinator/blob/master/src/main/kotlin/com/dasbikash/news_server_data_coordinator/article_data_uploader/DataUploaderForMongoRestService.kt)
: *DataUploader* implementation for datasource implementation using Mongo Db.

<strong>N.B.:</strong> For status monitoring and operation control of this app, another [`REST`](https://github.com/dasBikash84/ns_reloaded_data_coordinator_rest_service)
service has been implemented.
