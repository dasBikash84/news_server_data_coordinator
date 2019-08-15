package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import com.google.firebase.database.Exclude
import org.hibernate.Session
import javax.persistence.*

@Entity
@Table(name = DatabaseTableNames.FAV_PAGE_ENTRY_ON_USER_SETTINGS_TABLE_NAME)
data class FavPageEntryOnUserSettings(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Exclude
        var id: Int = 0,
        @Transient
        var pageId: String = "",
        var subscribed: Boolean = false
) {
    @ManyToOne(targetEntity = Page::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "pageId")
    @Exclude
    var page: Page? = null

    @ManyToOne(targetEntity = FirebaseUser::class, fetch = FetchType.EAGER)
    @JoinColumn(name = "firebaseUserId")
    @Exclude
    var firebaseUser: FirebaseUser? = null

    @Exclude
    @Transient
    var firebaseUserId: String=""

    fun loadPage(session: Session) {
        DatabaseUtils.getAllPages(session)
                .find { it.id == pageId && it.topLevelPage!! }?.let {
                    page = it
                }
    }

    fun loadFirebaseUser(session: Session) {
        DatabaseUtils.getAllFirebaseUser(session)
                .find { it.uid == firebaseUserId }?.let {
                    firebaseUser = it
                }
    }

    override fun toString(): String {
        return "FavPageEntryOnUserSettings(id=$id, pageId='$pageId', subscribed=$subscribed, page=${page?.id}, " +
                "firebaseUser=${firebaseUser?.providerId}, firebaseUserId='$firebaseUserId')"
    }

}