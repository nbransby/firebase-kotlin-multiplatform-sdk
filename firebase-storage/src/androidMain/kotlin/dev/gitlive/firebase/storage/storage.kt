/*
 * Copyright (c) 2023 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

@file:JvmName("android")

package dev.gitlive.firebase.storage

import android.net.Uri
import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.storage.OnPausedListener
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseApp
import dev.gitlive.firebase.android as publicAndroid
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

public val FirebaseStorage.android: com.google.firebase.storage.FirebaseStorage get() = com.google.firebase.storage.FirebaseStorage.getInstance()

public actual val Firebase.storage: FirebaseStorage get() = FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance())

public actual fun Firebase.storage(url: String): FirebaseStorage = FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance(url))

public actual fun Firebase.storage(app: FirebaseApp): FirebaseStorage = FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance(app.publicAndroid))

public actual fun Firebase.storage(app: FirebaseApp, url: String): FirebaseStorage = FirebaseStorage(com.google.firebase.storage.FirebaseStorage.getInstance(app.publicAndroid, url))

public actual class FirebaseStorage(internal val android: com.google.firebase.storage.FirebaseStorage) {
    public actual val maxOperationRetryTime: Duration = android.maxOperationRetryTimeMillis.milliseconds
    public actual val maxUploadRetryTime: Duration = android.maxUploadRetryTimeMillis.milliseconds

    public actual fun setMaxOperationRetryTime(maxOperationRetryTime: Duration) {
        android.maxOperationRetryTimeMillis = maxOperationRetryTime.inWholeMilliseconds
    }

    public actual fun setMaxUploadRetryTime(maxUploadRetryTime: Duration) {
        android.maxUploadRetryTimeMillis = maxUploadRetryTime.inWholeMilliseconds
    }

    public actual fun useEmulator(host: String, port: Int) {
        android.useEmulator(host, port)
    }

    public actual val reference: StorageReference get() = StorageReference(android.reference)

    public actual fun reference(location: String): StorageReference = StorageReference(android.getReference(location))

    public actual fun getReferenceFromUrl(fullUrl: String): StorageReference = StorageReference(android.getReferenceFromUrl(fullUrl))
}

public val StorageReference.android: com.google.firebase.storage.StorageReference get() = android

public actual class StorageReference(internal val android: com.google.firebase.storage.StorageReference) {
    public actual val name: String get() = android.name
    public actual val path: String get() = android.path
    public actual val bucket: String get() = android.bucket
    public actual val parent: StorageReference? get() = android.parent?.let { StorageReference(it) }
    public actual val root: StorageReference get() = StorageReference(android.root)
    public actual val storage: FirebaseStorage get() = FirebaseStorage(android.storage)

    public actual suspend fun getMetadata(): FirebaseStorageMetadata? = android.metadata.await().toFirebaseStorageMetadata()

    public actual fun child(path: String): StorageReference = StorageReference(android.child(path))

    public actual suspend fun delete() {
        android.delete().await()
    }

    public actual suspend fun getDownloadUrl(): String = android.downloadUrl.await().toString()

    public actual suspend fun listAll(): ListResult = ListResult(android.listAll().await())

    public actual suspend fun putFile(file: File, metadata: FirebaseStorageMetadata?) {
        if (metadata != null) {
            android.putFile(file.uri, metadata.toStorageMetadata()).await().run {}
        } else {
            android.putFile(file.uri, FirebaseStorageMetadata().toStorageMetadata()).await().run {}
        }
    }

    public actual suspend fun putData(data: Data, metadata: FirebaseStorageMetadata?) {
        if (metadata != null) {
            android.putBytes(data.data, metadata.toStorageMetadata()).await().run {}
        } else {
            android.putBytes(data.data, FirebaseStorageMetadata().toStorageMetadata()).await().run {}
        }
    }

    public actual fun putFileResumable(file: File, metadata: FirebaseStorageMetadata?): ProgressFlow {
        val android = if (metadata != null) {
            android.putFile(file.uri, metadata.toStorageMetadata())
        } else {
            android.putFile(file.uri, FirebaseStorageMetadata().toStorageMetadata())
        }

        val flow = callbackFlow {
            val onCanceledListener = OnCanceledListener { cancel() }
            val onCompleteListener = OnCompleteListener<UploadTask.TaskSnapshot> { close(it.exception) }
            val onPausedListener = OnPausedListener<UploadTask.TaskSnapshot> { trySendBlocking(Progress.Paused(it.bytesTransferred, it.totalByteCount)) }
            val onProgressListener = OnProgressListener<UploadTask.TaskSnapshot> { trySendBlocking(Progress.Running(it.bytesTransferred, it.totalByteCount)) }
            android.addOnCanceledListener(onCanceledListener)
            android.addOnCompleteListener(onCompleteListener)
            android.addOnPausedListener(onPausedListener)
            android.addOnProgressListener(onProgressListener)
            awaitClose {
                android.removeOnCanceledListener(onCanceledListener)
                android.removeOnCompleteListener(onCompleteListener)
                android.removeOnPausedListener(onPausedListener)
                android.removeOnProgressListener(onProgressListener)
            }
        }

        return object : ProgressFlow {
            override suspend fun collect(collector: FlowCollector<Progress>) = collector.emitAll(flow)
            override fun pause() = android.pause().run {}
            override fun resume() = android.resume().run {}
            override fun cancel() = android.cancel().run {}
        }
    }
}

public val ListResult.android: com.google.firebase.storage.ListResult get() = android

public actual class ListResult(internal val android: com.google.firebase.storage.ListResult) {
    public actual val prefixes: List<StorageReference> = android.prefixes.map { StorageReference(it) }
    public actual val items: List<StorageReference> = android.items.map { StorageReference(it) }
    public actual val pageToken: String? = android.pageToken
}

public actual class File(public val uri: Uri)

public actual class Data(public val data: ByteArray)

public actual typealias FirebaseStorageException = com.google.firebase.storage.StorageException

internal fun FirebaseStorageMetadata.toStorageMetadata(): StorageMetadata = StorageMetadata.Builder()
    .setCacheControl(this.cacheControl)
    .setContentDisposition(this.contentDisposition)
    .setContentEncoding(this.contentEncoding)
    .setContentLanguage(this.contentLanguage)
    .setContentType(this.contentType)
    .apply {
        customMetadata.entries.forEach { (key, value) ->
            setCustomMetadata(key, value)
        }
    }.build()

internal fun StorageMetadata.toFirebaseStorageMetadata(): FirebaseStorageMetadata {
    val sdkMetadata = this
    return storageMetadata {
        md5Hash = sdkMetadata.md5Hash
        cacheControl = sdkMetadata.cacheControl
        contentDisposition = sdkMetadata.contentDisposition
        contentEncoding = sdkMetadata.contentEncoding
        contentLanguage = sdkMetadata.contentLanguage
        contentType = sdkMetadata.contentType
        sdkMetadata.customMetadataKeys.forEach {
            setCustomMetadata(it, sdkMetadata.getCustomMetadata(it))
        }
    }
}
