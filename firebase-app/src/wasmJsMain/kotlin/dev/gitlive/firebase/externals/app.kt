@file:JsModule("firebase/app")

package dev.gitlive.firebase.externals

import kotlin.js.Promise

external fun initializeApp(options: JsAny, name: String = definedExternally): FirebaseApp

external fun getApp(name: String = definedExternally): FirebaseApp

external fun getApps(): JsArray<FirebaseApp>

external fun deleteApp(app: FirebaseApp): Promise<JsAny?>

external interface FirebaseApp: JsAny {
    val automaticDataCollectionEnabled: Boolean
    val name: String
    val options: FirebaseOptions
}

external interface FirebaseOptions {
    val apiKey: String
    val appId : String
    val authDomain: String?
    val databaseURL: String?
    val measurementId: String?
    val messagingSenderId: String?
    val gaTrackingId: String?
    val projectId: String?
    val storageBucket: String?
}
