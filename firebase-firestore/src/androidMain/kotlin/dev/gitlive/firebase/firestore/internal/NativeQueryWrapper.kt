package dev.gitlive.firebase.firestore.internal

import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.Filter
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.WhereConstraint
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

internal actual open class NativeQueryWrapper internal actual constructor(actual open val native: Query) {

    actual fun limit(limit: Number) = native.limit(limit.toLong())

    actual val snapshots get() = callbackFlow {
        val listener = native.addSnapshotListener { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val metadataChanges =
            if (includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val listener = native.addSnapshotListener(metadataChanges) { snapshot, exception ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            exception?.let { close(exception) }
        }
        awaitClose { listener.remove() }
    }

    actual suspend fun get(source: Source): QuerySnapshot =
        QuerySnapshot(native.get(source.toAndroidSource()).await())

    actual fun where(filter: Filter) = native.where(filter.toAndroidFilter())

    private fun Filter.toAndroidFilter(): com.google.firebase.firestore.Filter = when (this) {
        is Filter.And -> com.google.firebase.firestore.Filter.and(
            *filters.map { it.toAndroidFilter() }
                .toTypedArray(),
        )
        is Filter.Or -> com.google.firebase.firestore.Filter.or(
            *filters.map { it.toAndroidFilter() }
                .toTypedArray(),
        )
        is Filter.Field -> {
            when (constraint) {
                is WhereConstraint.ForNullableObject -> {
                    val modifier: (String, Any?) -> com.google.firebase.firestore.Filter = when (constraint) {
                        is WhereConstraint.EqualTo -> com.google.firebase.firestore.Filter::equalTo
                        is WhereConstraint.NotEqualTo -> com.google.firebase.firestore.Filter::notEqualTo
                    }
                    modifier.invoke(field, constraint.value)
                }
                is WhereConstraint.ForObject -> {
                    val modifier: (String, Any) -> com.google.firebase.firestore.Filter = when (constraint) {
                        is WhereConstraint.LessThan -> com.google.firebase.firestore.Filter::lessThan
                        is WhereConstraint.GreaterThan -> com.google.firebase.firestore.Filter::greaterThan
                        is WhereConstraint.LessThanOrEqualTo -> com.google.firebase.firestore.Filter::lessThanOrEqualTo
                        is WhereConstraint.GreaterThanOrEqualTo -> com.google.firebase.firestore.Filter::greaterThanOrEqualTo
                        is WhereConstraint.ArrayContains -> com.google.firebase.firestore.Filter::arrayContains
                    }
                    modifier.invoke(field, constraint.value)
                }
                is WhereConstraint.ForArray -> {
                    val modifier: (String, List<Any>) -> com.google.firebase.firestore.Filter = when (constraint) {
                        is WhereConstraint.InArray -> com.google.firebase.firestore.Filter::inArray
                        is WhereConstraint.ArrayContainsAny -> com.google.firebase.firestore.Filter::arrayContainsAny
                        is WhereConstraint.NotInArray -> com.google.firebase.firestore.Filter::notInArray
                    }
                    modifier.invoke(field, constraint.values)
                }
            }
        }
        is Filter.Path -> {
            when (constraint) {
                is WhereConstraint.ForNullableObject -> {
                    val modifier: (FieldPath, Any?) -> com.google.firebase.firestore.Filter = when (constraint) {
                        is WhereConstraint.EqualTo -> com.google.firebase.firestore.Filter::equalTo
                        is WhereConstraint.NotEqualTo -> com.google.firebase.firestore.Filter::notEqualTo
                    }
                    modifier.invoke(path.android, constraint.value)
                }
                is WhereConstraint.ForObject -> {
                    val modifier: (FieldPath, Any) -> com.google.firebase.firestore.Filter = when (constraint) {
                        is WhereConstraint.LessThan -> com.google.firebase.firestore.Filter::lessThan
                        is WhereConstraint.GreaterThan -> com.google.firebase.firestore.Filter::greaterThan
                        is WhereConstraint.LessThanOrEqualTo -> com.google.firebase.firestore.Filter::lessThanOrEqualTo
                        is WhereConstraint.GreaterThanOrEqualTo -> com.google.firebase.firestore.Filter::greaterThanOrEqualTo
                        is WhereConstraint.ArrayContains -> com.google.firebase.firestore.Filter::arrayContains
                    }
                    modifier.invoke(path.android, constraint.value)
                }
                is WhereConstraint.ForArray -> {
                    val modifier: (FieldPath, List<Any>) -> com.google.firebase.firestore.Filter = when (constraint) {
                        is WhereConstraint.InArray -> com.google.firebase.firestore.Filter::inArray
                        is WhereConstraint.ArrayContainsAny -> com.google.firebase.firestore.Filter::arrayContainsAny
                        is WhereConstraint.NotInArray -> com.google.firebase.firestore.Filter::notInArray
                    }
                    modifier.invoke(path.android, constraint.values)
                }
            }
        }
    }

    actual fun orderBy(field: String, direction: Direction) = native.orderBy(field, direction)
    actual fun orderBy(field: EncodedFieldPath, direction: Direction) = native.orderBy(field, direction)

    actual fun startAfter(document: NativeDocumentSnapshot) = native.startAfter(document)
    actual fun startAfter(vararg fieldValues: Any) = native.startAfter(*fieldValues)
    actual fun startAt(document: NativeDocumentSnapshot) = native.startAt(document)
    actual fun startAt(vararg fieldValues: Any) = native.startAt(*fieldValues)

    actual fun endBefore(document: NativeDocumentSnapshot) = native.endBefore(document)
    actual fun endBefore(vararg fieldValues: Any) = native.endBefore(*fieldValues)
    actual fun endAt(document: NativeDocumentSnapshot) = native.endAt(document)
    actual fun endAt(vararg fieldValues: Any) = native.endAt(*fieldValues)

    private fun addSnapshotListener(
        includeMetadataChanges: Boolean = false,
        listener: ProducerScope<QuerySnapshot>.(com.google.firebase.firestore.QuerySnapshot?, com.google.firebase.firestore.FirebaseFirestoreException?) -> Unit,
    ) = callbackFlow {
        val executor = callbackExecutorMap[native.firestore] ?: TaskExecutors.MAIN_THREAD
        val metadataChanges =
            if (includeMetadataChanges) MetadataChanges.INCLUDE else MetadataChanges.EXCLUDE
        val registration =
            native.addSnapshotListener(executor, metadataChanges) { snapshots, exception ->
                listener(snapshots, exception)
            }
        awaitClose { registration.remove() }
    }
}
