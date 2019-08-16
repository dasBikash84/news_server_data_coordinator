package com.dasbikash.news_server_data_coordinator.firebase_user_info_synchronizer

import com.dasbikash.news_server_data_coordinator.database.DatabaseUtils
import com.dasbikash.news_server_data_coordinator.database.DbSessionManager
import com.dasbikash.news_server_data_coordinator.firebase.FireBaseConUtils
import com.dasbikash.news_server_data_coordinator.firebase.RealTimeDbUserSettingsUtils
import com.dasbikash.news_server_data_coordinator.model.db_entity.FirebaseUser
import com.dasbikash.news_server_data_coordinator.model.db_entity.FirebaseUserInfoSynchronizerLog
import com.dasbikash.news_server_data_coordinator.utils.DateUtils
import com.dasbikash.news_server_data_coordinator.utils.LoggerUtils
import com.google.firebase.auth.ExportedUserRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ListUsersPage
import org.hibernate.Session

class FirebaseUserSettingsSynchronizer:Thread() {

    private lateinit var dbSession:Session

    override fun run() {
        FireBaseConUtils.init()
        while (true) {
            DatabaseUtils.getLastFirebaseUserInfoSynchronizerLog(getDatabaseSession())?.let {
                val sleepTime = getUserCountDelendentSleepPeriod() - (System.currentTimeMillis() - it.created.time)
                if (sleepTime > 0) {
                    getDatabaseSession().close()
                    LoggerUtils.logOnDb("FirebaseUserInfoSynchronizer Going to sleep for: ${sleepTime/1000} secs",getDatabaseSession())
                    sleep(sleepTime)
                }
            }

            LoggerUtils.logOnDb("Starting new iteration of FirebaseUserInfoSynchronizer",getDatabaseSession())

            var page: ListUsersPage? = FirebaseAuth.getInstance().listUsers(null)
            var newUserCount = 0
            var syncedUserCount = 0
            val logMessageBuilder = StringBuilder("")
            while (page != null) {
                for (user in page.values) {

                    if (user != null && user.email != null || user.phoneNumber != null) {

                        val firebaseUserFromRemote = getFireBaseUserForRecord(user)
                        val firebaseUserFromDb = getDatabaseSession().get(FirebaseUser::class.java, user.uid)

                        var needSettingsUpdate = false

                        if (firebaseUserFromDb == null) {
                            DatabaseUtils.runDbTransection(getDatabaseSession()) { getDatabaseSession().save(firebaseUserFromRemote) }
                            needSettingsUpdate = (firebaseUserFromRemote.lastSettingsUpdateTime != null)
                            newUserCount++
                            if (logMessageBuilder.length > 0) {
                                logMessageBuilder.append(" | ")
                            }
                            logMessageBuilder.append("New user: ${user.uid}")
                            LoggerUtils.logOnDb("New user: ${user.uid}",getDatabaseSession())
                        } else if ((firebaseUserFromRemote.lastSettingsUpdateTime != null && firebaseUserFromDb.lastSettingsUpdateTime == null) ||
                                (firebaseUserFromRemote.lastSettingsUpdateTime != null &&
                                        firebaseUserFromRemote.lastSettingsUpdateTime!! > firebaseUserFromDb.lastSettingsUpdateTime!!)) {
                            firebaseUserFromDb.copyData(firebaseUserFromRemote)
                            DatabaseUtils.runDbTransection(getDatabaseSession()) { getDatabaseSession().update(firebaseUserFromDb) }
                            needSettingsUpdate = true
                            syncedUserCount++
                        }

                        if (needSettingsUpdate) {
                            DatabaseUtils.deleteFavPageEntriesOnUserSettingsForUser(getDatabaseSession(), firebaseUserFromRemote)
                            val serverFavEntries = RealTimeDbUserSettingsUtils.getFavEntriesForUser(user)
                                    .map {
                                        it.loadPage(getDatabaseSession())
                                        it.loadFirebaseUser(getDatabaseSession())
                                        it
                                    }.filter {
                                        it.page != null && it.firebaseUser != null
                                    }
                            if (serverFavEntries.isNotEmpty()) {
                                DatabaseUtils.runDbTransection(getDatabaseSession()) {
                                    serverFavEntries.forEach { getDatabaseSession().save(it) }
                                }
                            }
                            if (logMessageBuilder.length > 0) {
                                logMessageBuilder.append(" | ")
                            }
                            logMessageBuilder.append("Settings update for: ${user.uid}")
                        }
                    }
                }
                page = page.nextPage
            }
            val firebaseUserInfoSynchronizerLog =
                    FirebaseUserInfoSynchronizerLog(newUserCount = newUserCount, syncedUserCount = syncedUserCount, message = logMessageBuilder.toString())
            DatabaseUtils.runDbTransection(getDatabaseSession()) {getDatabaseSession().save(firebaseUserInfoSynchronizerLog)}
            getDatabaseSession().close()
        }
    }

    private fun getUserCountDelendentSleepPeriod(): Long {
        val currentUserCount = DatabaseUtils.getFirebaseUserCount(getDatabaseSession())

        if (currentUserCount < 100){
            return 30*DateUtils.ONE_MINUTE_IN_MS
        }else if (currentUserCount < 1000){
            return DateUtils.ONE_HOUR_IN_MS
        }else if (currentUserCount < 5000){
            return 3 * DateUtils.ONE_HOUR_IN_MS
        }else if (currentUserCount < 10000){
            return DateUtils.ONE_DAY_IN_MS
        }else{
            return 7*DateUtils.ONE_DAY_IN_MS
        }
    }

    private fun getFireBaseUserForRecord(user: ExportedUserRecord): FirebaseUser {
        val lastSettingsUpdateTime = RealTimeDbUserSettingsUtils.getLastSettingsUpdateTimeForUser(user)
        return FirebaseUser.getInstance(user,lastSettingsUpdateTime)
    }

    private fun getDatabaseSession(): Session {
        if (!::dbSession.isInitialized || !dbSession.isOpen) {
            dbSession = DbSessionManager.getNewSession()
        }
        return dbSession
    }
}