@file:JsModule("firebase/firestore")
@file:JsNonModule

package dev.gitlive.firebase.firestore.externals

import dev.gitlive.firebase.Unsubscribe
import dev.gitlive.firebase.externals.FirebaseApp
import kotlin.js.Json
import kotlin.js.Promise

public external fun documentId(): FieldPath

public external class FieldPath(vararg fieldNames: String) {
    public fun isEqual(other: FieldPath): Boolean
}

public external fun refEqual(left: DocumentReference, right: DocumentReference): Boolean

public external fun addDoc(reference: CollectionReference, data: Any): Promise<DocumentReference>

public external fun arrayRemove(vararg elements: Any): FieldValue

public external fun arrayUnion(vararg elements: Any): FieldValue

public external fun clearIndexedDbPersistence(firestore: Firestore): Promise<Unit>

public external fun collection(firestore: Firestore, collectionPath: String): CollectionReference

public external fun collection(reference: DocumentReference, collectionPath: String): CollectionReference

public external fun collectionGroup(firestore: Firestore, collectionId: String): Query

public external fun connectFirestoreEmulator(
    firestore: Firestore,
    host: String,
    port: Int,
    options: Any? = definedExternally,
)

public external fun deleteDoc(reference: DocumentReference): Promise<Unit>

public external fun deleteField(): FieldValue

public external fun disableNetwork(firestore: Firestore): Promise<Unit>

public external fun doc(firestore: Firestore, documentPath: String): DocumentReference

public external fun doc(firestore: CollectionReference, documentPath: String? = definedExternally): DocumentReference

public external fun enableIndexedDbPersistence(
    firestore: Firestore,
    persistenceSettings: Any? = definedExternally,
): Promise<Unit>

public external fun enableNetwork(firestore: Firestore): Promise<Unit>

public external fun endAt(document: DocumentSnapshot): QueryConstraint

public external fun endAt(vararg fieldValues: Any): QueryConstraint

public external fun endBefore(document: DocumentSnapshot): QueryConstraint

public external fun endBefore(vararg fieldValues: Any): QueryConstraint

public external fun getDoc(
    reference: DocumentReference,
    options: Any? = definedExternally,
): Promise<DocumentSnapshot>

public external fun getDocFromCache(
    reference: DocumentReference,
): Promise<DocumentSnapshot>

public external fun getDocFromServer(
    reference: DocumentReference,
): Promise<DocumentSnapshot>

public external fun getDocs(query: Query): Promise<QuerySnapshot>

public external fun getDocsFromCache(query: Query): Promise<QuerySnapshot>

public external fun getDocsFromServer(query: Query): Promise<QuerySnapshot>

public external fun getFirestore(app: FirebaseApp? = definedExternally, databaseId: String? = definedExternally): Firestore

public external fun increment(n: Int): FieldValue

public external fun initializeFirestore(app: FirebaseApp, settings: dynamic = definedExternally, databaseId: String? = definedExternally): Firestore

public external fun limit(limit: Number): QueryConstraint

public external fun onSnapshot(
    reference: DocumentReference,
    next: (snapshot: DocumentSnapshot) -> Unit,
    error: (error: Throwable) -> Unit,
): Unsubscribe

public external fun onSnapshot(
    reference: DocumentReference,
    options: Json,
    next: (snapshot: DocumentSnapshot) -> Unit,
    error: (error: Throwable) -> Unit,
): Unsubscribe

public external fun onSnapshot(
    reference: Query,
    next: (snapshot: QuerySnapshot) -> Unit,
    error: (error: Throwable) -> Unit,
): Unsubscribe

public external fun onSnapshot(
    reference: Query,
    options: Json,
    next: (snapshot: QuerySnapshot) -> Unit,
    error: (error: Throwable) -> Unit,
): Unsubscribe

public external fun orderBy(field: String, direction: Any): QueryConstraint

public external fun orderBy(field: FieldPath, direction: Any): QueryConstraint

public external fun query(query: Query, vararg queryConstraints: QueryConstraint): Query

public external fun <T> runTransaction(
    firestore: Firestore,
    updateFunction: (transaction: Transaction) -> Promise<T>,
    options: Any? = definedExternally,
): Promise<T>

public external fun serverTimestamp(): FieldValue

public external fun setDoc(
    documentReference: DocumentReference,
    data: Any,
    options: Any? = definedExternally,
): Promise<Unit>

public external fun setLogLevel(logLevel: String)

public external fun startAfter(document: DocumentSnapshot): QueryConstraint

public external fun startAfter(vararg fieldValues: Any): QueryConstraint

public external fun startAt(document: DocumentSnapshot): QueryConstraint

public external fun startAt(vararg fieldValues: Any): QueryConstraint

public external fun updateDoc(reference: DocumentReference, data: Any): Promise<Unit>

public external fun updateDoc(
    reference: DocumentReference,
    field: String,
    value: Any?,
    vararg moreFieldsAndValues: Any?,
): Promise<Unit>

public external fun updateDoc(
    reference: DocumentReference,
    field: FieldPath,
    value: Any?,
    vararg moreFieldsAndValues: Any?,
): Promise<Unit>

public external fun where(field: String, opStr: String, value: Any?): QueryConstraint

public external fun where(field: FieldPath, opStr: String, value: Any?): QueryConstraint

public external fun and(vararg queryConstraints: QueryConstraint): QueryConstraint

public external fun or(vararg queryConstraints: QueryConstraint): QueryConstraint

public external fun writeBatch(firestore: Firestore): WriteBatch

public external interface Firestore {
    public val app: FirebaseApp
}

public external class GeoPoint(latitude: Double, longitude: Double) {
    public val latitude: Double
    public val longitude: Double
    public fun isEqual(other: GeoPoint): Boolean
}

public external interface CollectionReference : Query {
    public val id: String
    public val path: String
    public val parent: DocumentReference?
}

public external interface DocumentChange {
    public val doc: DocumentSnapshot
    public val newIndex: Int
    public val oldIndex: Int
    public val type: String
}

public external class DocumentReference {
    public val id: String
    public val path: String
    public val parent: CollectionReference
}

public external interface DocumentSnapshot {
    public val id: String
    public val ref: DocumentReference
    public val metadata: SnapshotMetadata
    public fun data(options: Any? = definedExternally): Any?
    public fun exists(): Boolean
    public fun get(fieldPath: String, options: Any? = definedExternally): Any?
    public fun get(fieldPath: FieldPath, options: Any? = definedExternally): Any?
}

public external class FieldValue {
    public fun isEqual(other: FieldValue): Boolean
}

public external interface Query

public external interface QueryConstraint

public external interface QuerySnapshot {
    public val docs: Array<DocumentSnapshot>
    public val empty: Boolean
    public val metadata: SnapshotMetadata
    public fun docChanges(): Array<DocumentChange>
}

public external interface SnapshotMetadata {
    public val hasPendingWrites: Boolean
    public val fromCache: Boolean
}

public external interface Transaction {
    public fun get(documentReference: DocumentReference): Promise<DocumentSnapshot>

    public fun set(
        documentReference: DocumentReference,
        data: Any,
        options: Any? = definedExternally,
    ): Transaction

    public fun update(documentReference: DocumentReference, data: Any): Transaction

    public fun update(
        documentReference: DocumentReference,
        field: String,
        value: Any?,
        vararg moreFieldsAndValues: Any?,
    ): Transaction

    public fun update(
        documentReference: DocumentReference,
        field: FieldPath,
        value: Any?,
        vararg moreFieldsAndValues: Any?,
    ): Transaction

    public fun delete(documentReference: DocumentReference): Transaction
}

public external interface WriteBatch {
    public fun commit(): Promise<Unit>

    public fun delete(documentReference: DocumentReference): WriteBatch

    public fun set(
        documentReference: DocumentReference,
        data: Any,
        options: Any? = definedExternally,
    ): WriteBatch

    public fun update(documentReference: DocumentReference, data: Any): WriteBatch

    public fun update(
        documentReference: DocumentReference,
        field: String,
        value: Any?,
        vararg moreFieldsAndValues: Any?,
    ): WriteBatch

    public fun update(
        documentReference: DocumentReference,
        field: FieldPath,
        value: Any?,
        vararg moreFieldsAndValues: Any?,
    ): WriteBatch
}

public external class Timestamp(seconds: Double, nanoseconds: Double) {
    public companion object {
        public fun now(): Timestamp
    }

    public val seconds: Double
    public val nanoseconds: Double
    public fun toMillis(): Double

    public fun isEqual(other: Timestamp): Boolean
}

public external interface FirestoreLocalCache {
    public val kind: String
}

public external interface MemoryLocalCache : FirestoreLocalCache
public external interface PersistentLocalCache : FirestoreLocalCache

public external interface MemoryCacheSettings {
    public val garbageCollector: MemoryGarbageCollector
}

public external interface MemoryGarbageCollector {
    public val kind: String
}

public external interface MemoryLruGarbageCollector : MemoryGarbageCollector
public external interface MemoryEagerGarbageCollector : MemoryGarbageCollector

public external interface PersistentCacheSettings {
    public val cacheSizeBytes: Int
    public val tabManager: PersistentTabManager
}

public external interface PersistentTabManager {
    public val kind: String
}

public external fun memoryLocalCache(settings: MemoryCacheSettings): MemoryLocalCache
public external fun memoryEagerGarbageCollector(): MemoryEagerGarbageCollector
public external fun memoryLruGarbageCollector(settings: dynamic = definedExternally): MemoryLruGarbageCollector
public external fun persistentLocalCache(settings: PersistentCacheSettings): PersistentLocalCache
public external fun persistentSingleTabManager(settings: dynamic = definedExternally): PersistentTabManager
public external fun persistentMultipleTabManager(): PersistentTabManager
