/*
 * Copyright (c) 2020 GitLive Ltd.  Use of this source code is governed by the Apache 2.0 license.
 */

package dev.gitlive.firebase.internal

import dev.gitlive.firebase.nativeAssertEquals
import dev.gitlive.firebase.nativeListOf
import dev.gitlive.firebase.nativeMapOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.jvm.JvmInline
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@Suppress("unused")
@Serializable
object TestObject {
    val map = mapOf("key" to "value", "key2" to 12, "key3" to null)
    const val BOOL = false
    val nullableBool: Boolean? = null
}

@Serializable
@JvmInline
value class ValueClass(val int: Int)

@Serializable
data class TestData(
    val map: Map<String, String>,
    val otherMap: Map<Int, Int>,
    val bool: Boolean = false,
    val nullableBool: Boolean? = null,
    val valueClass: ValueClass,
)

@Serializable
sealed class SealedClass {
    @Serializable
    @SerialName("test")
    data class Test(val value: String) : SealedClass()
}

@Serializable
data class GenericClass<T>(
    val inner: T,
)

@Serializable
abstract class AbstractClass {
    abstract val abstractValue: String
}

@Serializable
@SerialName("implemented")
data class ImplementedClass(override val abstractValue: String, val otherValue: Boolean) : AbstractClass()

@Serializable
data class NestedClass(
    val testData: TestData,
    val sealed: SealedClass,
    val abstract: AbstractClass,
    val testDataList: List<TestData>,
    val sealedList: List<SealedClass>,
    val abstractList: List<AbstractClass>,
    val testDataMap: Map<TestData, TestData>,
    val sealedMap: Map<SealedClass, SealedClass>,
    val abstractMap: Map<AbstractClass, AbstractClass>,
)

class EncodersTest {

    @Test
    fun encodeDecodePrimaryTypes() {
        assertEncode(true)
        assertEncode(42)
        assertEncode(8.toShort())
        assertEncode(Int.MAX_VALUE.toLong() + 3)
        assertEncode(0x03F)
        assertEncode(3.33)
        assertEncode(6.65f)
        assertEncode("Test")
    }

    @Test
    fun encodeDecodeList() {
        val list = listOf("One", "Two", "Three")
        val encoded = encode<List<String>>(list) { encodeDefaults = true }

        nativeAssertEquals(nativeListOf("One", "Two", "Three"), encoded)

        val decoded = decode(ListSerializer(String.serializer()), encoded)
        assertEquals(listOf("One", "Two", "Three"), decoded)
    }

    @Test
    fun encodeDecodeNullableList() {
        val list = listOf("One", "Two", null)
        val encoded = encode<List<String?>>(list) { encodeDefaults = true }

        nativeAssertEquals(nativeListOf("One", "Two", null), encoded)

        val decoded = decode(ListSerializer(String.serializer().nullable), encoded)
        assertEquals(listOf("One", "Two", null), decoded)
    }

    @Test
    fun encodeDecodeMap() {
        val map = mapOf("key" to "value", "key2" to "value2", "key3" to "value3")
        val encoded = encode<Map<String, String>>(map) { encodeDefaults = true }

        nativeAssertEquals(nativeMapOf("key" to "value", "key2" to "value2", "key3" to "value3"), encoded)

        val decoded = decode(MapSerializer(String.serializer(), String.serializer()), encoded)
        assertEquals(mapOf("key" to "value", "key2" to "value2", "key3" to "value3"), decoded)
    }

    @Test
    fun encodeDecodeNullableMap() {
        val map = mapOf("key" to "value", "key2" to "value2", "key3" to null)
        val encoded = encode<Map<String, String?>>(map) { encodeDefaults = true }

        nativeAssertEquals(nativeMapOf("key" to "value", "key2" to "value2", "key3" to null), encoded)

        val decoded = decode(MapSerializer(String.serializer(), String.serializer().nullable), encoded)
        assertEquals(mapOf("key" to "value", "key2" to "value2", "key3" to null), decoded)
    }

    @Test
    fun encodeDecodeObject() {
        val encoded = encode(TestObject.serializer(), TestObject) { encodeDefaults = false }
        nativeAssertEquals(nativeMapOf(), encoded)

        val decoded = decode(TestObject.serializer(), encoded)
        assertEquals(TestObject, decoded)
    }

    @Test
    fun encodeDecodeValueClass() {
        val testValueClass = ValueClass(42)
        val encoded = encode(ValueClass.serializer(), testValueClass) { encodeDefaults = false }

        nativeAssertEquals(42, encoded)

        val decoded = decode(ValueClass.serializer(), encoded)
        assertEquals(testValueClass, decoded)
    }

    @Test
    fun encodeDecodeClass() {
        val testDataClass = TestData(mapOf("key" to "value"), mapOf(1 to 1), true, null, ValueClass(42))
        val encoded = encode(TestData.serializer(), testDataClass) { encodeDefaults = false }

        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "valueClass" to 42), encoded)

        val decoded = decode(TestData.serializer(), encoded)
        assertEquals(testDataClass, decoded)
    }

    @Test
    fun encodeDecodeClassNullableValue() {
        val testDataClass = TestData(mapOf("key" to "value"), mapOf(1 to 1), true, nullableBool = true, ValueClass(42))
        val encoded = encode(TestData.serializer(), testDataClass) { encodeDefaults = true }

        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to true, "valueClass" to 42), encoded)

        val decoded = decode(TestData.serializer(), encoded)
        assertEquals(testDataClass, decoded)
    }

    @Test
    fun encodeDecodeGenericClass() {
        val innerClass = TestData(mapOf("key" to "value"), mapOf(1 to 1), true, valueClass = ValueClass(42))
        val genericClass = GenericClass(innerClass)
        val encoded = encode(GenericClass.serializer(TestData.serializer()), genericClass) { encodeDefaults = true }

        nativeAssertEquals(nativeMapOf("inner" to nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to null, "valueClass" to 42)), encoded)

        val decoded = decode(GenericClass.serializer(TestData.serializer()), encoded)
        assertEquals(genericClass, decoded)
    }

    @Test
    fun encodeDecodeSealedClass() {
        val sealedClass = SealedClass.Test("value")
        val encoded = encode(SealedClass.serializer(), sealedClass) { encodeDefaults = true }

        nativeAssertEquals(nativeMapOf("type" to "test", "value" to "value"), encoded)

        val decoded = decode(SealedClass.serializer(), encoded)
        assertEquals(sealedClass, decoded)
    }

    @Test
    fun encodeDecodePolymorphicClass() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, AbstractClass.serializer()) {
                subclass(ImplementedClass::class, ImplementedClass.serializer())
            }
        }
        val abstractClass: AbstractClass = ImplementedClass("value", true)
        val encoded =
            encode(AbstractClass.serializer(), abstractClass) {
                encodeDefaults = true
                serializersModule = module
            }

        nativeAssertEquals(nativeMapOf("type" to "implemented", "abstractValue" to "value", "otherValue" to true), encoded)

        val decoded = decode(AbstractClass.serializer(), encoded) {
            serializersModule = module
        }
        assertEquals(abstractClass, decoded)
    }

    @Test
    fun encodeDecodeNestedClass() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, AbstractClass.serializer()) {
                subclass(ImplementedClass::class, ImplementedClass.serializer())
            }
        }

        val testData = TestData(mapOf("key" to "value"), mapOf(1 to 1), true, null, ValueClass(42))
        val sealedClass: SealedClass = SealedClass.Test("value")
        val abstractClass: AbstractClass = ImplementedClass("value", true)
        val nestedClass = NestedClass(testData, sealedClass, abstractClass, listOf(testData), listOf(sealedClass), listOf(abstractClass), mapOf(testData to testData), mapOf(sealedClass to sealedClass), mapOf(abstractClass to abstractClass))
        val encoded = encode(NestedClass.serializer(), nestedClass) {
            encodeDefaults = true
            serializersModule = module
        }

        val testDataEncoded = nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to null, "valueClass" to 42)
        val sealedEncoded = nativeMapOf("type" to "test", "value" to "value")
        val abstractEncoded = nativeMapOf("type" to "implemented", "abstractValue" to "value", "otherValue" to true)
        nativeAssertEquals(
            nativeMapOf(
                "testData" to testDataEncoded,
                "sealed" to sealedEncoded,
                "abstract" to abstractEncoded,
                "testDataList" to nativeListOf(testDataEncoded),
                "sealedList" to nativeListOf(sealedEncoded),
                "abstractList" to nativeListOf(abstractEncoded),
                "testDataMap" to nativeMapOf(testDataEncoded to testDataEncoded),
                "sealedMap" to nativeMapOf(sealedEncoded to sealedEncoded),
                "abstractMap" to nativeMapOf(abstractEncoded to abstractEncoded),
            ),
            encoded,
        )

        val decoded = decode(NestedClass.serializer(), encoded) {
            serializersModule = module
        }
        assertEquals(nestedClass, decoded)
    }

    @Test
    fun encodeDecodeNestedClassWithEmptyCollections() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, AbstractClass.serializer()) {
                subclass(ImplementedClass::class, ImplementedClass.serializer())
            }
        }

        val testData = TestData(mapOf(), mapOf(), true, null, ValueClass(42))
        val sealedClass: SealedClass = SealedClass.Test("value")
        val abstractClass: AbstractClass = ImplementedClass("value", true)
        val nestedClass = NestedClass(testData, sealedClass, abstractClass, listOf(), listOf(), listOf(), mapOf(), mapOf(), mapOf())
        val encoded = encode(NestedClass.serializer(), nestedClass) {
            encodeDefaults = true
            serializersModule = module
        }

        val testDataEncoded = nativeMapOf("map" to nativeMapOf(), "otherMap" to nativeMapOf(), "bool" to true, "nullableBool" to null, "valueClass" to 42)
        val sealedEncoded = nativeMapOf("type" to "test", "value" to "value")
        val abstractEncoded = nativeMapOf("type" to "implemented", "abstractValue" to "value", "otherValue" to true)
        nativeAssertEquals(
            nativeMapOf(
                "testData" to testDataEncoded,
                "sealed" to sealedEncoded,
                "abstract" to abstractEncoded,
                "testDataList" to nativeListOf(),
                "sealedList" to nativeListOf(),
                "abstractList" to nativeListOf(),
                "testDataMap" to nativeMapOf(),
                "sealedMap" to nativeMapOf(),
                "abstractMap" to nativeMapOf(),
            ),
            encoded,
        )

        val decoded = decode(NestedClass.serializer(), encoded) {
            serializersModule = module
        }
        assertEquals(nestedClass, decoded)
    }

    @Test
    fun reencodeTransformationList() {
        val reencoded = reencodeTransformation<List<String>>(nativeListOf("One", "Two", "Three")) {
            assertEquals(listOf("One", "Two", "Three"), it)
            it.map { value -> "new$value" }
        }
        nativeAssertEquals(nativeListOf("newOne", "newTwo", "newThree"), reencoded)
    }

    @Test
    fun reencodeTransformationMap() {
        val reencoded = reencodeTransformation<Map<String, String>>(nativeMapOf("key" to "value", "key2" to "value2", "key3" to "value3")) {
            assertEquals(mapOf("key" to "value", "key2" to "value2", "key3" to "value3"), it)
            it.mapValues { (_, value) -> "new-$value" }
        }

        nativeAssertEquals(nativeMapOf("key" to "new-value", "key2" to "new-value2", "key3" to "new-value3"), reencoded)
    }

    @Test
    fun reencodeTransformationObject() {
        val reencoded = reencodeTransformation<TestObject>(nativeMapOf(), { encodeDefaults = false }) {
            assertEquals(TestObject, it)
            it
        }
        nativeAssertEquals(nativeMapOf(), reencoded)
    }

    @Test
    fun reencodeTransformationValueClass() {
        val reencoded = reencodeTransformation<ValueClass>(
            42,
            { encodeDefaults = false },
        ) {
            assertEquals(ValueClass(42), it)
            ValueClass(23)
        }

        nativeAssertEquals(23, reencoded)
    }

    @Test
    fun reencodeTransformationClass() {
        val reencoded = reencodeTransformation<TestData>(
            nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to true, "valueClass" to 42),
            { encodeDefaults = false },
        ) {
            assertEquals(TestData(mapOf("key" to "value"), mapOf(1 to 1), bool = true, nullableBool = true, ValueClass(42)), it)
            it.copy(map = mapOf("newKey" to "newValue"), nullableBool = null)
        }

        nativeAssertEquals(nativeMapOf("map" to nativeMapOf("newKey" to "newValue"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "valueClass" to 42), reencoded)
    }

    @Test
    fun reencodeTransformationNullableValue() {
        val reencoded = reencodeTransformation<TestData?>(
            nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to true, "valueClass" to 42),
            { encodeDefaults = false },
        ) {
            assertEquals(TestData(mapOf("key" to "value"), mapOf(1 to 1), bool = true, nullableBool = true, valueClass = ValueClass(42)), it)
            null
        }

        nativeAssertEquals(null, reencoded)
    }

    @Test
    fun reencodeTransformationGenericClass() {
        val reencoded = reencodeTransformation(
            GenericClass.serializer(TestData.serializer()),
            nativeMapOf("inner" to nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to false, "valueClass" to 42)),
            { encodeDefaults = false },
        ) {
            assertEquals(
                GenericClass(TestData(mapOf("key" to "value"), mapOf(1 to 1), bool = true, nullableBool = false, valueClass = ValueClass(42))),
                it,
            )
            GenericClass(it.inner.copy(map = mapOf("newKey" to "newValue"), nullableBool = null))
        }

        nativeAssertEquals(nativeMapOf("inner" to nativeMapOf("map" to nativeMapOf("newKey" to "newValue"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "valueClass" to 42)), reencoded)
    }

    @Test
    fun reencodeTransformationSealedClass() {
        val reencoded = reencodeTransformation(SealedClass.serializer(), nativeMapOf("type" to "test", "value" to "value")) {
            assertEquals(SealedClass.Test("value"), it)
            SealedClass.Test("newTest")
        }

        nativeAssertEquals(nativeMapOf("type" to "test", "value" to "newTest"), reencoded)
    }

    @Test
    fun reencodeTransformationPolymorphicClass() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, AbstractClass.serializer()) {
                subclass(ImplementedClass::class, ImplementedClass.serializer())
            }
        }

        val reencoded = reencodeTransformation(
            AbstractClass.serializer(),
            nativeMapOf("type" to "implemented", "abstractValue" to "value", "otherValue" to true),
            builder = {
                serializersModule = module
            },
        ) {
            assertEquals(ImplementedClass("value", true), it)
            ImplementedClass("new-${it.abstractValue}", false)
        }

        nativeAssertEquals(nativeMapOf("type" to "implemented", "abstractValue" to "new-value", "otherValue" to false), reencoded)
    }

    @Test
    fun reencodeTransformationNestedClass() {
        val module = SerializersModule {
            polymorphic(AbstractClass::class, AbstractClass.serializer()) {
                subclass(ImplementedClass::class, ImplementedClass.serializer())
            }
        }

        val testData = TestData(mapOf("key" to "value"), mapOf(1 to 1), true, null, ValueClass(42))
        val sealedClass: SealedClass = SealedClass.Test("value")
        val abstractClass: AbstractClass = ImplementedClass("value", true)
        val nestedClass = NestedClass(testData, sealedClass, abstractClass, listOf(testData), listOf(sealedClass), listOf(abstractClass), mapOf(testData to testData), mapOf(sealedClass to sealedClass), mapOf(abstractClass to abstractClass))
        val encoded = encode(NestedClass.serializer(), nestedClass) {
            encodeDefaults = true
            serializersModule = module
        }

        val reencoded = reencodeTransformation(NestedClass.serializer(), encoded, builder = {
            encodeDefaults = true
            serializersModule = module
        }) {
            assertEquals(nestedClass, it)
            it.copy(sealed = SealedClass.Test("newValue"))
        }

        val testDataEncoded = nativeMapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "nullableBool" to null, "valueClass" to 42)
        val sealedEncoded = nativeMapOf("type" to "test", "value" to "value")
        val abstractEncoded = nativeMapOf("type" to "implemented", "abstractValue" to "value", "otherValue" to true)
        nativeAssertEquals(
            nativeMapOf(
                "testData" to testDataEncoded,
                "sealed" to nativeMapOf("type" to "test", "value" to "newValue"),
                "abstract" to abstractEncoded,
                "testDataList" to nativeListOf(testDataEncoded),
                "sealedList" to nativeListOf(sealedEncoded),
                "abstractList" to nativeListOf(abstractEncoded),
                "testDataMap" to nativeMapOf(testDataEncoded to testDataEncoded),
                "sealedMap" to nativeMapOf(sealedEncoded to sealedEncoded),
                "abstractMap" to nativeMapOf(abstractEncoded to abstractEncoded),
            ),
            reencoded,
        )
    }

    @Test
    fun encodeAsObject() {
        val testDataClass = TestData(mapOf("key" to "value"), mapOf(1 to 1), true, null, ValueClass(42))
        val encodedObject = encodeAsObject(
            TestData.serializer(),
            testDataClass,
        ) { encodeDefaults = false }

        nativeAssertEquals(mapOf("map" to nativeMapOf("key" to "value"), "otherMap" to nativeMapOf(1 to 1), "bool" to true, "valueClass" to 42), encodedObject.getRaw())

        val testMap = mapOf("one" to 1, "two" to null, "three" to false)
        assertEquals(testMap, encodeAsObject(testMap).getRaw())

        assertEquals(emptyMap(), encodeAsObject(TestObject).getRaw())

        assertFailsWith<IllegalArgumentException> {
            encodeAsObject(
                true,
            )
        }
        assertFailsWith<IllegalArgumentException> { encodeAsObject(42) }
        assertFailsWith<IllegalArgumentException> { encodeAsObject(8.toShort()) }
        assertFailsWith<IllegalArgumentException> { encodeAsObject(Int.MAX_VALUE.toLong() + 3) }
        assertFailsWith<IllegalArgumentException> {
            encodeAsObject(
                0x03F,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            encodeAsObject(
                3.33,
            )
        }
        assertFailsWith<IllegalArgumentException> {
            encodeAsObject(
                6.65f,
            )
        }
        assertFailsWith<IllegalArgumentException> { encodeAsObject("Test") }
        assertFailsWith<IllegalArgumentException> {
            encodeAsObject(
                ValueClass(2),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            encodeAsObject(
                mapOf(1 to "one"),
            )
        }
        assertFailsWith<IllegalArgumentException> {
            encodeAsObject(
                listOf("one"),
            )
        }
    }

    private inline fun <reified T> assertEncode(value: T) {
        val encoded = encode(value)
        assertEquals(value, encoded)
        assertEquals(value, decode<T>(encoded))

        val nullableEncoded = encode<T?>(null)
        assertNull(nullableEncoded)
        assertNull(decode<T?>(nullableEncoded))
    }
}
