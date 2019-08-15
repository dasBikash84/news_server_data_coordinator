package com.dasbikash.news_server_data_coordinator.model.db_entity

import com.dasbikash.news_server_data_coordinator.model.DatabaseTableNames
import com.google.firebase.auth.ExportedUserRecord
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = DatabaseTableNames.FIREBASE_USER_ENTRY_NAME)
data class FirebaseUser(
        @Id
        var uid:String="",
        var providerId:String?=null,
        var disabled:Boolean=false,
        var displayName:String?=null,
        var emailId:String?=null,
        var phoneNumber:String?=null,
        var emailVerified:Boolean=false,
        var photoUrl:String?=null,
        var lastSettingsUpdateTime:Date?=null
){
    fun copyData(updatedInstance:FirebaseUser){
        providerId=updatedInstance.providerId
        disabled=updatedInstance.disabled
        displayName=updatedInstance.displayName
        emailId=updatedInstance.emailId
        phoneNumber=updatedInstance.phoneNumber
        emailVerified=updatedInstance.emailVerified
        photoUrl=updatedInstance.photoUrl
        lastSettingsUpdateTime=updatedInstance.lastSettingsUpdateTime
    }

    override fun toString(): String {
        return "FirebaseUser(uid='$uid', providerId=$providerId, disabled=$disabled, displayName=$displayName, emailId=$emailId, phoneNumber=$phoneNumber, " +
                "emailVerified=$emailVerified, photoUrl=$photoUrl, lastSettingsUpdateTime=${lastSettingsUpdateTime?.time})"
    }


    companion object{
        fun getInstance(exportedUserRecord: ExportedUserRecord ,lastSettingsUpdateTime:Date?=null) =
                FirebaseUser(
                        uid = exportedUserRecord.uid,
                        providerId = exportedUserRecord.providerData.map { it.providerId }.joinToString(separator = " | ",postfix = "",prefix = ""),
                        disabled = exportedUserRecord.isDisabled,
                        displayName = exportedUserRecord.displayName,
                        emailId = exportedUserRecord.email,
                        phoneNumber = exportedUserRecord.phoneNumber,
                        emailVerified = exportedUserRecord.isEmailVerified,
                        photoUrl = exportedUserRecord.photoUrl,
                        lastSettingsUpdateTime = lastSettingsUpdateTime
                )
    }
}