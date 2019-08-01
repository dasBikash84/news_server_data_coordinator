/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.model.db_entity.*
import com.google.cloud.firestore.annotation.Exclude
import java.util.*

class LanguageForFB(
        val id:String,
        var name: String){
    companion object{
        fun getFromLanguage(language: Language)
                =LanguageForFB(language.id,language.name!!)
    }
}

class CountryForFB(
        val name: String,
        val countryCode: String,
        val timeZone: String){
    companion object{
        fun getFromCountry(country: Country)
                =CountryForFB(country.name,country.countryCode!!,country.timeZone!!)
    }
}
class NewspaperForFB(
        val id: String,
        val name: String,
        val countryName: String,
        val languageId: String,
        val active:Boolean
){
    companion object{
        fun getFromNewspaper(newspaper: Newspaper) =
                NewspaperForFB(newspaper.id,newspaper.name!!,newspaper.country!!.name,
                        newspaper.language!!.id,newspaper.active)
    }
}

class PageForFB(
        val id: String,
        val newspaperId: String,
        val parentPageId: String,
        val name: String,
        var hasData: Boolean?=null,
        var hasChild: Boolean?=null,
        var topLevelPage: Boolean?=null,
        val active:Boolean
){
    companion object{
        fun getFromPage(page: Page) =
                PageForFB(page.id,page.newspaper!!.id,page.parentPageId!!,page.name!!,
                            page.hasData,page.hasChild,page.topLevelPage,page.active)
    }

}

class PageGroupForFB(
        val name: String,
        val active: Boolean,
        val pageList: List<String>
){
    companion object{
        fun getFromPageGroup(pageGroup: PageGroup) =
                PageGroupForFB(pageGroup.name!!,pageGroup.active,pageGroup.pageList!!.map { it.id }.toList())
    }

}
class ArticleForFB(
        val id: String,
        val pageId: String,
        val title: String,
        val publicationTime: Date,
        @Exclude
        private var publicationTimeRTDB: Long? = null,
        val articleText: String,
        val imageLinkList: List<ArticleImage>,
        val previewImageLink: String?
){
    companion object{
        fun fromArticle(article: Article):ArticleForFB{
            return ArticleForFB(article.id,article.page!!.id,article.title!!,article.publicationTime!!,null,
                                article.articleText!!,article.imageLinkList,article.previewImageLink)
        }
    }
    @Exclude
    fun getPublicationTimeRTDB():Long{
        return publicationTime.time
    }

    override fun toString(): String {
        return "ArticleForFB(id='$id', title='$title', publicationTimeRTDB=${publicationTimeRTDB})"
    }

}
class ArticleForRTDB(
        val id: String,
        val pageId: String,
        val title: String,
        val publicationTime: Date,
        private var publicationTimeRTDB: Long? = null,
        val articleText: String,
        val imageLinkList: List<ArticleImage>,
        val previewImageLink: String?
){
    companion object{
        fun fromArticle(article: Article):ArticleForRTDB{
            if (article.page!!.topLevelPage!!) {
                return ArticleForRTDB(article.id, article.page!!.id, article.title!!, article.publicationTime!!, null,
                        article.articleText!!, article.imageLinkList, article.previewImageLink)
            }else{
                return ArticleForRTDB(article.id, article.page!!.parentPageId!!, article.title!!, article.publicationTime!!, null,
                        article.articleText!!, article.imageLinkList, article.previewImageLink)
            }
        }
    }

    fun getPublicationTimeRTDB():Long{
        return publicationTime.time
    }

    override fun toString(): String {
        return "ArticleForRTDB(id='$id', pageId='$pageId', title='$title', publicationTime=$publicationTime, publicationTimeRTDB=$publicationTimeRTDB, previewImageLink=$previewImageLink)"
    }


}

