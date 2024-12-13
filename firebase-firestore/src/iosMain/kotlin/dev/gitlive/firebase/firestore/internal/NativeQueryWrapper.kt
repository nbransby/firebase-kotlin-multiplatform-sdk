package dev.gitlive.firebase.firestore.internal

import cocoapods.FirebaseFirestoreInternal.FIRFilter
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.Filter
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.NativeQuery
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.WhereConstraint
import dev.gitlive.firebase.firestore.awaitResult
import dev.gitlive.firebase.firestore.toException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import platform.Foundation.NSNull

internal actual open class NativeQueryWrapper internal actual constructor(actual open val native: NativeQuery) {

    actual fun limit(limit: Number) = native.queryLimitedTo(limit.toLong())

    actual suspend fun get(source: Source) =
        QuerySnapshot(awaitResult { native.getDocumentsWithSource(source.toIosSource(), it) })

    actual val snapshots get() = callbackFlow {
        val listener = native.addSnapshotListener { snapshot, error ->
            snapshot?.let { trySend(QuerySnapshot(snapshot)) }
            error?.let { close(error.toException()) }
        }
        awaitClose { listener.remove() }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val listener =
            native.addSnapshotListenerWithIncludeMetadataChanges(includeMetadataChanges) { snapshot, error ->
                snapshot?.let { trySend(QuerySnapshot(snapshot)) }
                error?.let { close(error.toException()) }
            }
        awaitClose { listener.remove() }
    }

    actual fun where(filter: Filter) = native.queryWhereFilter(filter.toFIRFilter())

    private fun Filter.toFIRFilter(): FIRFilter = when (this) {
        is Filter.And -> FIRFilter.andFilterWithFilters(filters.map { it.toFIRFilter() })
        is Filter.Or -> FIRFilter.orFilterWithFilters(filters.map { it.toFIRFilter() })
        is Filter.Field -> when (constraint) {
            is WhereConstraint.EqualTo -> FIRFilter.filterWhereField(field, isEqualTo = constraint.value ?: NSNull.`null`())
            is WhereConstraint.NotEqualTo -> FIRFilter.filterWhereField(field, isNotEqualTo = constraint.value ?: NSNull.`null`())
            is WhereConstraint.LessThan -> FIRFilter.filterWhereField(field, isLessThan = constraint.value)
            is WhereConstraint.GreaterThan -> FIRFilter.filterWhereField(field, isGreaterThan = constraint.value)
            is WhereConstraint.LessThanOrEqualTo -> FIRFilter.filterWhereField(field, isLessThanOrEqualTo = constraint.value)
            is WhereConstraint.GreaterThanOrEqualTo -> FIRFilter.filterWhereField(field, isGreaterThanOrEqualTo = constraint.value)
            is WhereConstraint.ArrayContains -> FIRFilter.filterWhereField(field, arrayContains = constraint.value)
            is WhereConstraint.ArrayContainsAny -> FIRFilter.filterWhereField(field, arrayContainsAny = constraint.values)
            is WhereConstraint.InArray -> FIRFilter.filterWhereField(field, `in` = constraint.values)
            is WhereConstraint.NotInArray -> FIRFilter.filterWhereField(field, notIn = constraint.values)
        }
        is Filter.Path -> when (constraint) {
            is WhereConstraint.EqualTo -> FIRFilter.filterWhereFieldPath(path.ios, isEqualTo = constraint.value ?: NSNull.`null`())
            is WhereConstraint.NotEqualTo -> FIRFilter.filterWhereFieldPath(path.ios, isNotEqualTo = constraint.value ?: NSNull.`null`())
            is WhereConstraint.LessThan -> FIRFilter.filterWhereFieldPath(path.ios, isLessThan = constraint.value)
            is WhereConstraint.GreaterThan -> FIRFilter.filterWhereFieldPath(path.ios, isGreaterThan = constraint.value)
            is WhereConstraint.LessThanOrEqualTo -> FIRFilter.filterWhereFieldPath(path.ios, isLessThanOrEqualTo = constraint.value)
            is WhereConstraint.GreaterThanOrEqualTo -> FIRFilter.filterWhereFieldPath(path.ios, isGreaterThanOrEqualTo = constraint.value)
            is WhereConstraint.ArrayContains -> FIRFilter.filterWhereFieldPath(path.ios, arrayContains = constraint.value)
            is WhereConstraint.ArrayContainsAny -> FIRFilter.filterWhereFieldPath(path.ios, arrayContainsAny = constraint.values)
            is WhereConstraint.InArray -> FIRFilter.filterWhereFieldPath(path.ios, `in` = constraint.values)
            is WhereConstraint.NotInArray -> FIRFilter.filterWhereFieldPath(path.ios, notIn = constraint.values)
        }
    }

    actual fun orderBy(field: String, direction: Direction) = native.queryOrderedByField(field, direction == Direction.DESCENDING)
    actual fun orderBy(field: EncodedFieldPath, direction: Direction) = native.queryOrderedByFieldPath(field, direction == Direction.DESCENDING)

    actual fun startAfter(document: NativeDocumentSnapshot) = native.queryStartingAfterDocument(document)
    actual fun startAfter(vararg fieldValues: Any) = native.queryStartingAfterValues(fieldValues.asList())
    actual fun startAt(document: NativeDocumentSnapshot) = native.queryStartingAtDocument(document)
    actual fun startAt(vararg fieldValues: Any) = native.queryStartingAtValues(fieldValues.asList())

    actual fun endBefore(document: NativeDocumentSnapshot) = native.queryEndingBeforeDocument(document)
    actual fun endBefore(vararg fieldValues: Any) = native.queryEndingBeforeValues(fieldValues.asList())
    actual fun endAt(document: NativeDocumentSnapshot) = native.queryEndingAtDocument(document)
    actual fun endAt(vararg fieldValues: Any) = native.queryEndingAtValues(fieldValues.asList())
}
