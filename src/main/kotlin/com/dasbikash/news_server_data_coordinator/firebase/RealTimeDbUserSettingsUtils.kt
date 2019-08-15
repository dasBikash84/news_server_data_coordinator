package com.dasbikash.news_server_data_coordinator.firebase

import com.dasbikash.news_server_data_coordinator.model.db_entity.FavPageEntryOnUserSettings
import com.google.firebase.auth.ExportedUserRecord
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.*

object RealTimeDbUserSettingsUtils {

    fun getLastSettingsUpdateTimeForUser(userRecord: ExportedUserRecord):Date?{

        val lock = Object()
        var lastUpdateTime:Date? = null

        RealTimeDbRefUtils.getUserSettingsRef()
                .child(userRecord.uid)
                .child("updateLog")
                .orderByChild("timeStamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError?) {
                        synchronized(lock){lock.notify()}
                    }

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        snapshot?.let {
                            if (it.exists() && it.hasChildren()) {
                                val calander = Calendar.getInstance()
                                var timeStamp = it.children.first().getValue(UpdateLogEntry::class.java).timeStamp
                                timeStamp = (timeStamp/1000)*1000
                                calander.timeInMillis = timeStamp
                                lastUpdateTime = calander.time
                            }
                        }
                        synchronized(lock){lock.notify()}
                    }
                })
        synchronized(lock){lock.wait(30000)}
        return lastUpdateTime
    }

    fun getFavEntriesForUser(userRecord: ExportedUserRecord): List<FavPageEntryOnUserSettings> {

        val lock = Object()
        val favPageEntriesOnUserSettings = mutableListOf<FavPageEntryOnUserSettings>()

        RealTimeDbRefUtils.getUserSettingsRef()
                .child(userRecord.uid)
                .child("favPageEntryMap")
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError?) {
                        synchronized(lock){lock.notify()}
                    }

                    override fun onDataChange(snapshot: DataSnapshot?) {
                        snapshot?.let {
                            if (it.exists() && it.hasChildren()) {
                                it.children.asSequence().forEach {
                                    val favPageEntryOnUserSettings = it.getValue(FavPageEntryOnUserSettings::class.java)
                                    favPageEntryOnUserSettings.firebaseUserId = userRecord.uid
                                    favPageEntriesOnUserSettings.add(favPageEntryOnUserSettings)
                                }
                            }
                        }
                        synchronized(lock){lock.notify()}
                    }
                })
        synchronized(lock){lock.wait(30000)}
        return favPageEntriesOnUserSettings.toList()
    }
}

data class UpdateLogEntry(
        var timeStamp:Long = 0
)