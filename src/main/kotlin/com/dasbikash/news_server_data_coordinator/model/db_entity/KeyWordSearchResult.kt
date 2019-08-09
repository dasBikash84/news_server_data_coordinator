package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import org.hibernate.Session
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
        const val PAGE_ID_INDEX = 0
        const val ARTICLE_ID_INDEX = 1
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
        return "KeyWordSearchResult(keyWord=$keyWord, searchResult=${searchResult}, " +
                "modified=$modified, lastUploadedOnFireBaseDb = $lastUploadedOnFireBaseDb)"
    }

    fun getSearchResultMap(session: Session): Map<String, ArticleSearchResultDbEntry?> {
        val resultMap = mutableMapOf<String, ArticleSearchResultDbEntry?>()
        if (searchResult.isNotBlank()) {
            searchResult.split(ENTRY_SEPERATOR).asSequence().forEach {
                val data = it.split(PAGE_ID_ARTICLE_ID_SEPERATOR).toList()
                if (data.size == 2) {
                    val article = DatabaseUtils.findArticleById(session, data.get(ARTICLE_ID_INDEX))!!
                    if (article.deletedFromFireStore && article.deletedFromFirebaseDb) {
//                    if (DatabaseUtils.checkIfArticleDeleted(session,data.get(ARTICLE_ID_INDEX))){
                        resultMap.put(data.get(ARTICLE_ID_INDEX), null)
                    } else {
                        val page = DatabaseUtils.findPageById(session, data.get(PAGE_ID_INDEX))!!
                        if (page.topLevelPage!!) {
                            resultMap.put(data.get(ARTICLE_ID_INDEX), ArticleSearchResultDbEntry(page.id, article.publicationTime!!.time))
                        } else {
                            resultMap.put(data.get(ARTICLE_ID_INDEX), ArticleSearchResultDbEntry(page.parentPageId!!, article.publicationTime!!.time))
                        }
                    }
                }
            }
        }
        val searchResultBuilder = StringBuilder("")
        resultMap.keys.asSequence().forEach {
            if (resultMap.get(it) != null) {
                if (searchResultBuilder.isNotBlank()) {
                    searchResultBuilder.append(ENTRY_SEPERATOR)
                }
                searchResultBuilder
                        .append(resultMap.get(it)!!.pageId)
                        .append(PAGE_ID_ARTICLE_ID_SEPERATOR)
                        .append(it)
            }
        }
        searchResult = searchResultBuilder.toString()
        DatabaseUtils.runDbTransection(session) { session.update(this) }
        return resultMap.toMap()
    }

}

data class ArticleSearchResultDbEntry(
        val pageId: String,
        val publicationTs: Long
)