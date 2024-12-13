package dev.gitlive.firebase.firestore

import dev.gitlive.firebase.EncodeSettings
import dev.gitlive.firebase.copyFrom
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

public sealed interface WhereConstraint {

    public sealed interface ForNullableObject : WhereConstraint {
        public val value: Any?
    }

    public sealed interface ForObject : WhereConstraint {
        public val value: Any
    }

    public sealed interface ForArray : WhereConstraint {
        public val values: List<Any>
    }

    public data class EqualTo @PublishedApi internal constructor(private val valueBuilder: () -> Any?) : ForNullableObject {
        override val value: Any? get() = valueBuilder()
    }

    public data class NotEqualTo @PublishedApi internal constructor(private val valueBuilder: () -> Any?) : ForNullableObject {
        override val value: Any? get() = valueBuilder()
    }

    public data class LessThan @PublishedApi internal constructor(private val valueBuilder: () -> Any) : ForObject {
        override val value: Any get() = valueBuilder()
    }

    public data class GreaterThan @PublishedApi internal constructor(private val valueBuilder: () -> Any) : ForObject {
        override val value: Any get() = valueBuilder()
    }

    public data class LessThanOrEqualTo @PublishedApi internal constructor(private val valueBuilder: () -> Any) : ForObject {
        override val value: Any get() = valueBuilder()
    }

    public data class GreaterThanOrEqualTo @PublishedApi internal constructor(private val valueBuilder: () -> Any) : ForObject {
        override val value: Any get() = valueBuilder()
    }

    public data class ArrayContains @PublishedApi internal constructor(private val valueBuilder: () -> Any) : ForObject {
        override val value: Any get() = valueBuilder()
    }

    public data class ArrayContainsAny @PublishedApi internal constructor(private val valueBuilders: List<() -> Any>) : ForArray {
        override val values: List<Any> get() = valueBuilders.map { it.invoke() }
    }

    public data class InArray @PublishedApi internal constructor(private val valueBuilders: List<() -> Any>) : ForArray {
        override val values: List<Any> get() = valueBuilders.map { it.invoke() }
    }

    public data class NotInArray @PublishedApi internal constructor(private val valueBuilders: List<() -> Any>) : ForArray {
        override val values: List<Any> get() = valueBuilders.map { it.invoke() }
    }
}

public sealed class Filter {
    public data class And internal constructor(val filters: List<Filter>) : Filter()
    public data class Or internal constructor(val filters: List<Filter>) : Filter()
    public sealed class WithConstraint : Filter() {
        public abstract val constraint: WhereConstraint
    }

    public data class Field @PublishedApi internal constructor(val field: String, override val constraint: WhereConstraint) : WithConstraint()
    public data class Path @PublishedApi internal constructor(val path: FieldPath, override val constraint: WhereConstraint) : WithConstraint()
}

public class FilterBuilder internal constructor() : EncodeSettings.Builder {

    override var encodeDefaults: Boolean = true
    override var serializersModule: SerializersModule = EmptySerializersModule()

    public fun withEncoder(dsl: FilterBuilder.() -> Filter): Filter = FilterBuilder()
        .run(dsl)

    public val String.isNull: Filter.WithConstraint get() = Filter.Field(this, WhereConstraint.EqualTo { null })
    public inline infix fun <reified T> String.equalTo(value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.EqualTo {
            encode(value) { copyFrom(this@FilterBuilder) }
        },
    )
    public fun <T : Any> String.equalTo(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.EqualTo {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }
        },
    )

    public val FieldPath.isNull: Filter.WithConstraint get() = Filter.Path(this, WhereConstraint.EqualTo { null })
    public inline infix fun <reified T> FieldPath.equalTo(value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.EqualTo {
            encode(value) { copyFrom(this@FilterBuilder) }
        },
    )
    public fun <T : Any> FieldPath.equalTo(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.EqualTo {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }
        },
    )

    public val String.isNotNull: Filter.WithConstraint get() = Filter.Field(this, WhereConstraint.NotEqualTo { null })
    public inline infix fun <reified T> String.notEqualTo(value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.NotEqualTo {
            encode(value) { copyFrom(this@FilterBuilder) }
        },
    )
    public fun <T : Any> String.notEqualTo(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.NotEqualTo {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }
        },
    )

    public val FieldPath.isNotNull: Filter.WithConstraint get() = Filter.Path(this, WhereConstraint.NotEqualTo { null })
    public inline infix fun <reified T> FieldPath.notEqualTo(value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.NotEqualTo {
            encode(value) { copyFrom(this@FilterBuilder) }
        },
    )
    public fun <T : Any> FieldPath.notEqualTo(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.NotEqualTo {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }
        },
    )

    public inline infix fun <reified T : Any> String.lessThan(value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.LessThan {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> String.lessThan(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.LessThan {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> FieldPath.lessThan(value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.LessThan {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> FieldPath.lessThan(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.LessThan {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> String.greaterThan(value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.GreaterThan {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> String.greaterThan(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.GreaterThan {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> FieldPath.greaterThan(value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.GreaterThan {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> FieldPath.greaterThan(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.GreaterThan {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> String.lessThanOrEqualTo(value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.LessThanOrEqualTo {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> String.lessThanOrEqualTo(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.LessThanOrEqualTo {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> FieldPath.lessThanOrEqualTo(value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.LessThanOrEqualTo {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> FieldPath.lessThanOrEqualTo(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.LessThanOrEqualTo {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> String.greaterThanOrEqualTo(value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.GreaterThanOrEqualTo {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> String.greaterThanOrEqualTo(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.GreaterThanOrEqualTo {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> FieldPath.greaterThanOrEqualTo(value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.GreaterThanOrEqualTo {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> FieldPath.greaterThanOrEqualTo(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.GreaterThanOrEqualTo {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> String.contains(value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.ArrayContains {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> String.contains(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.ArrayContains {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> FieldPath.contains(value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.ArrayContains {
            encode(value) { copyFrom(this@FilterBuilder) }!!
        },
    )
    public fun <T : Any> FieldPath.contains(strategy: SerializationStrategy<T>, value: T): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.ArrayContains {
            dev.gitlive.firebase.internal.encode(strategy, value) { copyFrom(this@FilterBuilder) }!!
        },
    )

    public inline infix fun <reified T : Any> String.containsAny(values: List<T>): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.ArrayContainsAny(
            values.map { value ->
                { encode(value) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )
    public fun <T : Any> String.containsAny(strategy: SerializationStrategy<T>, values: List<T>): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.ArrayContainsAny(
            values.map {
                { dev.gitlive.firebase.internal.encode(strategy, it) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )

    public inline infix fun <reified T : Any> FieldPath.containsAny(values: List<T>): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.ArrayContainsAny(
            values.map { value ->
                { encode(value) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )
    public fun <T : Any> FieldPath.containsAny(strategy: SerializationStrategy<T>, values: List<T>): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.ArrayContainsAny(
            values.map {
                { dev.gitlive.firebase.internal.encode(strategy, it) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )

    public inline infix fun <reified T : Any> String.inArray(values: List<T>): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.InArray(
            values.map { value ->
                { encode(value) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )
    public fun <T : Any> String.inArray(strategy: SerializationStrategy<T>, values: List<T>): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.InArray(
            values.map {
                { dev.gitlive.firebase.internal.encode(strategy, it) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )

    public inline infix fun <reified T : Any> FieldPath.inArray(values: List<T>): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.InArray(
            values.map { value ->
                { encode(value) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )
    public fun <T : Any> FieldPath.inArray(strategy: SerializationStrategy<T>, values: List<T>): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.InArray(
            values.map {
                { dev.gitlive.firebase.internal.encode(strategy, it) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )

    public inline infix fun <reified T : Any> String.notInArray(values: List<T>): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.NotInArray(
            values.map { value ->
                { encode(value) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )
    public fun <T : Any> String.notInArray(strategy: SerializationStrategy<T>, values: List<T>): Filter.WithConstraint = Filter.Field(
        this,
        WhereConstraint.NotInArray(
            values.map {
                { dev.gitlive.firebase.internal.encode(strategy, it) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )

    public inline infix fun <reified T : Any> FieldPath.notInArray(values: List<T>): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.NotInArray(
            values.map { value ->
                { encode(value) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )
    public fun <T : Any> FieldPath.notInArray(strategy: SerializationStrategy<T>, values: List<T>): Filter.WithConstraint = Filter.Path(
        this,
        WhereConstraint.NotInArray(
            values.map {
                { dev.gitlive.firebase.internal.encode(strategy, it) { copyFrom(this@FilterBuilder) }!! }
            },
        ),
    )

    public infix fun Filter.and(right: Filter): Filter.And {
        val leftList = when (this) {
            is Filter.And -> filters
            else -> listOf(this)
        }
        val rightList = when (right) {
            is Filter.And -> right.filters
            else -> listOf(right)
        }
        return Filter.And(leftList + rightList)
    }

    public infix fun Filter.or(right: Filter): Filter.Or {
        val leftList = when (this) {
            is Filter.Or -> filters
            else -> listOf(this)
        }
        val rightList = when (right) {
            is Filter.Or -> right.filters
            else -> listOf(right)
        }
        return Filter.Or(leftList + rightList)
    }

    public fun all(vararg filters: Filter): Filter? = filters.toList().combine { left, right -> left and right }
    public fun any(vararg filters: Filter): Filter? = filters.toList().combine { left, right -> left or right }

    private fun Collection<Filter>.combine(over: (Filter, Filter) -> Filter): Filter? = fold<Filter, Filter?>(null) { acc, filter ->
        acc?.let { over(acc, filter) } ?: filter
    }
}
