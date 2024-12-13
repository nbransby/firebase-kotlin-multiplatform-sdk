package dev.gitlive.firebase.firestore.internal

import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.EncodedFieldPath
import dev.gitlive.firebase.firestore.Filter
import dev.gitlive.firebase.firestore.NativeDocumentSnapshot
import dev.gitlive.firebase.firestore.NativeQuery
import dev.gitlive.firebase.firestore.QuerySnapshot
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.WhereConstraint
import dev.gitlive.firebase.firestore.errorToException
import dev.gitlive.firebase.firestore.externals.Query
import dev.gitlive.firebase.firestore.externals.QueryConstraint
import dev.gitlive.firebase.firestore.externals.and
import dev.gitlive.firebase.firestore.externals.getDocs
import dev.gitlive.firebase.firestore.externals.getDocsFromCache
import dev.gitlive.firebase.firestore.externals.getDocsFromServer
import dev.gitlive.firebase.firestore.externals.onSnapshot
import dev.gitlive.firebase.firestore.externals.or
import dev.gitlive.firebase.firestore.externals.query
import dev.gitlive.firebase.firestore.rethrow
import dev.gitlive.firebase.firestore.wrapped
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlin.js.json

internal actual open class NativeQueryWrapper internal actual constructor(actual open val native: NativeQuery) {

    constructor(js: Query) : this(NativeQuery(js))

    open val js: Query get() = native.js

    actual suspend fun get(source: Source) = rethrow { QuerySnapshot(js.get(source).await()) }

    actual fun limit(limit: Number) = query(
        js,
        dev.gitlive.firebase.firestore.externals.limit(limit),
    ).wrapped

    actual fun where(filter: Filter) = query(js, filter.toQueryConstraint()).wrapped

    private fun Filter.toQueryConstraint(): QueryConstraint = when (this) {
        is Filter.And -> and(*filters.map { it.toQueryConstraint() }.toTypedArray())
        is Filter.Or -> or(*filters.map { it.toQueryConstraint() }.toTypedArray())
        is Filter.Field -> {
            val value = when (constraint) {
                is WhereConstraint.ForNullableObject -> constraint.value
                is WhereConstraint.ForObject -> constraint.value
                is WhereConstraint.ForArray -> constraint.values.toTypedArray()
            }
            dev.gitlive.firebase.firestore.externals.where(field, constraint.filterOp, value)
        }
        is Filter.Path -> {
            val value = when (constraint) {
                is WhereConstraint.ForNullableObject -> constraint.value
                is WhereConstraint.ForObject -> constraint.value
                is WhereConstraint.ForArray -> constraint.values.toTypedArray()
            }
            dev.gitlive.firebase.firestore.externals.where(path.js, constraint.filterOp, value)
        }
    }

    private val WhereConstraint.filterOp: String get() = when (this) {
        is WhereConstraint.EqualTo -> "=="
        is WhereConstraint.NotEqualTo -> "!="
        is WhereConstraint.LessThan -> "<"
        is WhereConstraint.LessThanOrEqualTo -> "<="
        is WhereConstraint.GreaterThan -> ">"
        is WhereConstraint.GreaterThanOrEqualTo -> ">="
        is WhereConstraint.ArrayContains -> "array-contains"
        is WhereConstraint.ArrayContainsAny -> "array-contains-any"
        is WhereConstraint.InArray -> "in"
        is WhereConstraint.NotInArray -> "not-in"
    }

    actual fun orderBy(field: String, direction: Direction) = rethrow {
        query(js, dev.gitlive.firebase.firestore.externals.orderBy(field, direction.jsString)).wrapped
    }

    actual fun orderBy(field: EncodedFieldPath, direction: Direction) = rethrow {
        query(js, dev.gitlive.firebase.firestore.externals.orderBy(field, direction.jsString)).wrapped
    }

    actual fun startAfter(document: NativeDocumentSnapshot) = rethrow {
        query(
            js,
            dev.gitlive.firebase.firestore.externals.startAfter(document.js),
        ).wrapped
    }

    actual fun startAfter(vararg fieldValues: Any) = rethrow {
        query(
            js,
            dev.gitlive.firebase.firestore.externals.startAfter(*fieldValues),
        ).wrapped
    }

    actual fun startAt(document: NativeDocumentSnapshot) = rethrow {
        query(
            js,
            dev.gitlive.firebase.firestore.externals.startAt(document.js),
        ).wrapped
    }

    actual fun startAt(vararg fieldValues: Any) = rethrow {
        query(
            js,
            dev.gitlive.firebase.firestore.externals.startAt(*fieldValues),
        ).wrapped
    }

    actual fun endBefore(document: NativeDocumentSnapshot) = rethrow {
        query(
            js,
            dev.gitlive.firebase.firestore.externals.endBefore(document.js),
        ).wrapped
    }

    actual fun endBefore(vararg fieldValues: Any) = rethrow {
        query(
            js,
            dev.gitlive.firebase.firestore.externals.endBefore(*fieldValues),
        ).wrapped
    }

    actual fun endAt(document: NativeDocumentSnapshot) = rethrow {
        query(
            js,
            dev.gitlive.firebase.firestore.externals.endAt(document.js),
        ).wrapped
    }

    actual fun endAt(vararg fieldValues: Any) = rethrow {
        query(
            js,
            dev.gitlive.firebase.firestore.externals.endAt(*fieldValues),
        ).wrapped
    }

    actual val snapshots get() = callbackFlow {
        val unsubscribe = rethrow {
            onSnapshot(
                js,
                { trySend(QuerySnapshot(it)) },
                { close(errorToException(it)) },
            )
        }
        awaitClose { rethrow { unsubscribe() } }
    }

    actual fun snapshots(includeMetadataChanges: Boolean) = callbackFlow {
        val unsubscribe = rethrow {
            onSnapshot(
                js,
                json("includeMetadataChanges" to includeMetadataChanges),
                { trySend(QuerySnapshot(it)) },
                { close(errorToException(it)) },
            )
        }
        awaitClose { rethrow { unsubscribe() } }
    }
}

private fun Query.get(source: Source) = when (source) {
    Source.DEFAULT -> getDocs(this)
    Source.CACHE -> getDocsFromCache(this)
    Source.SERVER -> getDocsFromServer(this)
}
