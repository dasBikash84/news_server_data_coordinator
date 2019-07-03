package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import java.util.*
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.KEY_WORD_SERACH_RESULT_TABLE_NAME)
data class KeyWordSearchResult(
        @Id
        var keyWord: String? = null,

        @Column(columnDefinition = "MEDIUMTEXT")
        var searchResult: String = "",

        @Temporal(TemporalType.TIMESTAMP)
        @Column(nullable = false, updatable = false, insertable = false)
        var modified: Date? = null,

        var lastUploadedOnFireBaseDb: Date? = null,
        var lastUploadedOnFireStore: Date? = null,
        var lastUploadedOnMongoRestService: Date? = null
) {
    companion object {
        const val ENTRY_SEPERATOR = " | "
        const val PAGE_ID_ARTICLE_ID_SEPERATOR = ","
    }

    fun addArticleInfo(article: Article) {

        val searchResultBuilder = StringBuilder(searchResult)

        if (searchResult.isNotBlank()) {
            searchResultBuilder.append(ENTRY_SEPERATOR)
        }
        searchResultBuilder
                .append(article.page!!.id)
                .append(PAGE_ID_ARTICLE_ID_SEPERATOR)
                .append(article.id)

        searchResult = searchResultBuilder.toString()
    }

    override fun toString(): String {
        return "KeyWordSearchResult(keyWord=$keyWord, searchResult='$searchResult', modified=$modified)"
    }

    fun getSearchResultMap():Map<String,String>{
        val resultMap = mutableMapOf<String,String>()
        if (searchResult.isNotBlank()){
            searchResult.split(ENTRY_SEPERATOR).asSequence().forEach {
                val data = it.split(PAGE_ID_ARTICLE_ID_SEPERATOR).toList()
                if (data.size == 2){
                    resultMap.put(data.get(1),data.get(0))
                }
            }
        }
        return resultMap.toMap()
    }

}