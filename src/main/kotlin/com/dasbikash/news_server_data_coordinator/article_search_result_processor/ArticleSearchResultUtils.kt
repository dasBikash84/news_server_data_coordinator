package com.dasbikash.news_server_data_coordinator.article_search_result_processor

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.firebase.RealTimeDbDataUtils
import com.dasbikash.news_server_data_coordinator.model.db_entity.Article
import com.dasbikash.news_server_data_coordinator.model.db_entity.KeyWordSearchResult
import com.dasbikash.news_server_data_coordinator.model.db_entity.RestrictedSearchKeyWord
import org.hibernate.Session
import java.util.*

internal object ArticleSearchResultUtils {

    private val restrictedSearchKeyWordList = mutableListOf<RestrictedSearchKeyWord>()

    private const val BANGLA_UNICODE_ZERO: Char = 0x09E6.toChar()
    private const val BANGLA_UNICODE_NINE: Char = 0x09EF.toChar()
    private const val ENGLISH_UNICODE_ZERO: Char = 0x0030.toChar()
    private const val ENGLISH_UNICODE_NINE: Char = 0x0039.toChar()
    private const val MINIMUM_KEYWORD_LENGTH = 3

    private fun replaceBanglaDigits(string: String): String {
        val chars = string.toCharArray()

        for (i in chars.indices) {
            val ch = chars[i]
            if (ch <= BANGLA_UNICODE_NINE && ch >= BANGLA_UNICODE_ZERO) {
                chars[i] = ch + ENGLISH_UNICODE_ZERO.toInt() - BANGLA_UNICODE_ZERO.toInt()
            }
        }
        return String(chars)
    }

    private fun getRestrictedSearchKeyWordList(session: Session):List<RestrictedSearchKeyWord>{
        if (restrictedSearchKeyWordList.isEmpty()){
            restrictedSearchKeyWordList.addAll(DatabaseUtils.getAllRestrictedSearchKeyWord(session))
        }
        return restrictedSearchKeyWordList.toList()
    }

    private fun checkIfKeyWordRestricted(session: Session,keyWord:String):Boolean{
        if (keyWord.isBlank()){
            return true
        }
        return getRestrictedSearchKeyWordList(session).find { it.keyWord.equals(keyWord.trim(),true) } !=null
    }

    fun processArticleForSearchResult(session: Session,article: Article){

        if (article.processedForSearchResult){
            return
        }
        var title = article.title!!
//        LoggerUtils.logOnConsole(title)
        title = replaceBanglaDigits(title)
        title = title.replace(Regex("\\p{Punct}|’|‘|\\d")," ")

        title.split(Regex("\\s+")).asSequence()
                .map { it.trim() }
                .filter { it.length >= MINIMUM_KEYWORD_LENGTH && !checkIfKeyWordRestricted(session, it) }
                .forEach {
//                    LoggerUtils.logOnConsole(it)
                    val keyWordSearchResult =
                            session.get(KeyWordSearchResult::class.java,it) ?: KeyWordSearchResult(keyWord = it.toLowerCase())
                    keyWordSearchResult.addArticleInfo(article)
//                    LoggerUtils.logOnConsole(keyWordSearchResult.toString())
                    DatabaseUtils.runDbTransection(session){session.saveOrUpdate(keyWordSearchResult)}
                }
        article.processedForSearchResult = true
        DatabaseUtils.runDbTransection(session){session.saveOrUpdate(article)}
    }

    fun writeKeyWordSearchResults(keyWordSearchResults: List<KeyWordSearchResult>,session: Session){
        RealTimeDbDataUtils.uploadKeyWordSearchResultData(keyWordSearchResults,session)
        keyWordSearchResults.asSequence().forEach {
            it.lastUploadedOnFireBaseDb = Date()
            DatabaseUtils.runDbTransection(session){session.update(it)}
        }
    }
}