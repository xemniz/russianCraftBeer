package ru.xmn.russiancraftbeer.services.beer.data

import junit.framework.Assert
import org.junit.Test
import ru.xmn.common.extensions.ClassField
import ru.xmn.common.extensions.GsonTestResult
import ru.xmn.common.extensions.WrongClassField
import ru.xmn.common.extensions.test

class PubShortDataRealmTest {
    @Test
    fun `no extra fields`() {
        val actual = test(TwoFieldData::class.java, twoFieldsJson)
        val expected = GsonTestResult(emptyList(), emptyList())
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `no extra fields nested`() {
        val actual = test(OneFieldNestedData::class.java, oneFieldNestedJson)
        val expected = GsonTestResult(emptyList(), emptyList())
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `extra field in class`() {
        val actual = test(TwoFieldData::class.java, oneFieldJson)
        val expected = GsonTestResult(listOf(ClassField("TwoFieldData", "field2")), emptyList())
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `extra field in json`() {
        val actual = test(OneFieldData::class.java, twoFieldsJson)
        val expected = GsonTestResult(emptyList(), listOf(ClassField("OneFieldData", "field2")))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `extra in json nested`() {
        val actual = test(OneFieldNestedData::class.java, oneFieldTwoNestedJson)
        val expected = GsonTestResult(emptyList(), listOf(ClassField("OneFieldData", "field2")))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `extra in class nested`() {
        val actual = test(OneFieldTwoNestedData::class.java, oneFieldNestedJson)
        val expected = GsonTestResult(listOf(ClassField("TwoFieldData", "field2")), emptyList())
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `two extra in class nested`() {
        val actual = test(TwoFieldTwoThreeNestedData::class.java, twoFieldOneThreeNestedJson)
        val expected = GsonTestResult(listOf(ClassField("TwoFieldData", "field2"), ClassField("ThreeFieldData", "field3")), emptyList())
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `expected boolean but was string`() {
        val actual = test(OneFieldData::class.java, oneFieldJsonBool)
        val expected = GsonTestResult(
                wrongClassFields = listOf(
                        WrongClassField(
                                "OneFieldData",
                                "field1",
                                "String",
                                "Boolean")))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `expected list but was string`() {
        val actual = test(OneFieldData::class.java, oneFieldJsonList)
        val expected = GsonTestResult(
                wrongClassFields = listOf(
                        WrongClassField(
                                "OneFieldData",
                                "field1",
                                "String",
                                "Array")))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `expected number but was string`() {
        val actual = test(OneFieldData::class.java, oneFieldJsonNumber)
        val expected = GsonTestResult(
                wrongClassFields = listOf(
                        WrongClassField(
                                "OneFieldData",
                                "field1",
                                "String",
                                "Number")))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `expected object but was string`() {
        val actual = test(OneFieldData::class.java, oneFieldJsonObject)
        val expected = GsonTestResult(
                wrongClassFields = listOf(
                        WrongClassField(
                                "OneFieldData",
                                "field1",
                                "String",
                                "Object")))
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun `expected string but was boolean`() {
        val actual = test(OneFieldDataBoolean::class.java, oneFieldJson)
        val expected = GsonTestResult(
                wrongClassFields = listOf(
                        WrongClassField(
                                "OneFieldDataBoolean",
                                "field1",
                                "Boolean",
                                "String")))
        Assert.assertEquals(expected, actual)
    }
}

data class OneFieldData(val field1: String)
data class OneFieldDataBoolean(val field1: Boolean)
data class TwoFieldData(val field1: String, val field2: String)
data class ThreeFieldData(val field1: String, val field2: String, val field3: ThreeFieldData)
data class OneFieldNestedData(val field1: OneFieldData)
data class OneFieldTwoNestedData(val field1: TwoFieldData)
data class TwoFieldTwoThreeNestedData(val field1: TwoFieldData, val field2: ThreeFieldData)

val oneFieldJson = """
    { field1: "string" }
""".trimIndent()

val twoFieldsJson = """
    { field1: "string",
    field2: "string" }
""".trimIndent()

val oneFieldNestedJson = """
    { field1: { field1: "string" } }
""".trimIndent()

val oneFieldTwoNestedJson = """
    { field1:
        { field1: "string",
        field2: "string" } }
""".trimIndent()

val twoFieldOneThreeNestedJson = """
    { field1:
        { field1: "string" },
    field2:
        { field1: "string",
        field2: "string" } }
""".trimIndent()

val oneFieldJsonBool = """
    { field1: false }
""".trimIndent()

val oneFieldJsonNumber = """
    { field1: 1 }
""".trimIndent()

val oneFieldJsonList = """
    { field1: [1,2] }
""".trimIndent()

val oneFieldJsonObject = """
    { field1: { field1: 1 } }
""".trimIndent()