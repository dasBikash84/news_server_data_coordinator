package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.firebase.FireBaseConUtils
import com.dasbikash.news_server_data_coordinator.firebase.RealTimeDbUserSettingsUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ListUsersPage
import org.hibernate.Session
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class FirebaseUserTest {
    lateinit var session: Session
    @BeforeEach
    fun setUp() {
        session = DbSessionManager.getNewSession()
    }

    @AfterEach
    fun tearDown() {
        session.close()
    }

//    @Test
//    fun authTest(){
//        FireBaseConUtils.init()
//        var page: ListUsersPage? = FirebaseAuth.getInstance().listUsers(null)
//        while (page != null) {
//            for (user in page.values) {
//                if (user.email !=null || user.phoneNumber!=null) {
//                    println()
//                    val lastUpdateTime = RealTimeDbUserSettingsUtils.getLastSettingsUpdateTimeForUser(user)
//                    println(FirebaseUser.getInstance(user,lastUpdateTime))
//                    RealTimeDbUserSettingsUtils.getFavEntriesForUser(user).map {
//                        it.loadFirebaseUser(session)
//                        it.loadPage(session)
//                        it
//                    }.asSequence().forEach {
//                        println(it.toString())
//                    }
//                }
//            }
//            page = page.nextPage
//        }
//    }
}