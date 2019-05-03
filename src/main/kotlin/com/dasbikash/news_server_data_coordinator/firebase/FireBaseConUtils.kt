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

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.database.FirebaseDatabase
import java.io.FileInputStream

object FireBaseConUtils {

    private const val SERVICE_ACCOUNT_CONFIG_FILE_PATH = "dontTrack/newsserver-bdb31-firebase-adminsdk-x4lq3-7ab25285b7.json"
    val mFirebaseDatabaseCon: FirebaseDatabase
    val mFireStoreCon:Firestore

    init {
        val serviceAccount = FileInputStream(SERVICE_ACCOUNT_CONFIG_FILE_PATH)
        val credentials = GoogleCredentials.fromStream(serviceAccount)

        val options = FirebaseOptions.Builder()
                .setCredentials(credentials)
                .build()

        FirebaseApp.initializeApp(options)

        mFireStoreCon = FirestoreClient.getFirestore()
        mFirebaseDatabaseCon = FirebaseDatabase.getInstance("https://newsserver-bdb31.firebaseio.com/")
    }
}