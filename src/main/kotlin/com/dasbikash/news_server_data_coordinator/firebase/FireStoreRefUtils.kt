package com.dasbikash.news_server_data_coordinator.firebase

object FireStoreRefUtils {

    private const val LANGUAGES_COLLECTION_LABEL= "languages"
    private const val COUNTRIES_COLLECTION_LABEL = "countries"
    private const val NEWSPAPERS_COLLECTION_LABEL = "newspapers"
    private const val PAGES_COLLECTION_LABEL = "pages"
    private const val PAGE_GROUPS_COLLECTION_LABEL = "page_groups"
    private const val APP_SETTINGS_UPDATE_TIME__COLLECTION_LABEL = "update_time"

    private const val ARTICLE_COLLECTION_LABEL = "articles"

    internal fun getLanguageSettingsCollectionRef() =
            FireBaseConUtils.mFireStoreCon.collection(LANGUAGES_COLLECTION_LABEL)

    internal fun getCountrySettingsCollectionRef() =
            FireBaseConUtils.mFireStoreCon.collection(COUNTRIES_COLLECTION_LABEL)

    internal fun getNewspaperSettingsCollectionRef() =
            FireBaseConUtils.mFireStoreCon.collection(NEWSPAPERS_COLLECTION_LABEL)

    internal fun getPageSettingsCollectionRef() =
            FireBaseConUtils.mFireStoreCon.collection(PAGES_COLLECTION_LABEL)

    internal fun getPageGroupSettingsCollectionRef() =
            FireBaseConUtils.mFireStoreCon.collection(PAGE_GROUPS_COLLECTION_LABEL)

    internal fun getSettingsUpdateTimeCollectionRef() =
            FireBaseConUtils.mFireStoreCon.collection(APP_SETTINGS_UPDATE_TIME__COLLECTION_LABEL)

    internal fun getArticleCollectionRef() =
            FireBaseConUtils.mFireStoreCon.collection(ARTICLE_COLLECTION_LABEL)
}