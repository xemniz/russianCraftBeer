package ru.xmn.common.extensions

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import java.io.StringReader
import java.lang.reflect.Field

data class GsonTestResult(
        val classFieldsInClasses: List<ClassField> = emptyList(),
        val classFieldsInJson: List<ClassField> = emptyList(),
        val wrongClassFields: List<WrongClassField> = emptyList()
)

data class ClassField(val className: String, val fieldName: String)
data class WrongClassField(val className: String, val fieldName: String, val classType: String, val jsonType: String)

private operator fun GsonTestResult.plus(test: GsonTestResult) =
        copy(classFieldsInClasses = classFieldsInClasses + test.classFieldsInClasses,
                classFieldsInJson = classFieldsInJson + test.classFieldsInJson,
                wrongClassFields = wrongClassFields + test.wrongClassFields)

fun <T> test(clazz: Class<T>, json: String): GsonTestResult {
    val parser = JsonParser()
    val jsonObject = parser.parse(json).asJsonObject
    val declaredFields = clazz.declaredFields
    val gsonTestResult = declaredFields.fold(GsonTestResult()) { acc, field ->
        when {
            isSimpleType(field.type) || jsonObject.get(field.name) == null -> acc
            else -> acc + test(field.type, jsonObject.get(field.name).toString())
        }
    }
    val fieldNames = declaredFields.map { it.name }
    return GsonTestResult(
            classFieldsInClasses = fieldNames.filter { !jsonObject.has(it) }.map { ClassField(clazz.simpleName, it) },
            classFieldsInJson = jsonObject.keySet().filter { !fieldNames.contains(it) }.map { ClassField(clazz.simpleName, it) },
            wrongClassFields = getWrongTypes(clazz.simpleName, declaredFields, jsonObject)
    ) + gsonTestResult
}

fun getWrongTypes(className: String, declaredFields: Array<out Field>, jsonObject: JsonObject): List<WrongClassField> {
    return declaredFields.fold(ArrayList()) { acc, field ->
        if (jsonObject.get(field.name) == null) return@fold acc
        val reader = StringReader(jsonObject.get(field.name).toString())
        val jsonReader = JsonReader(reader)
        if (getTypeString(jsonReader.peek()) != getTypeString(field.type))
            acc.apply { add(WrongClassField(className, field.name, getTypeString(field.type), getTypeString(jsonReader.peek()!!))) }
        else acc
    }
}

enum class Types {
    String, Number, Boolean, Array, Object, Unknown
}

fun getTypeString(clazz: Class<*>): String {
    val type = when (clazz) {

        String::class.java -> {
            Types.String
        }
        Int::class.java,
        Long::class.java,
        Float::class.java,
        Double::class.java -> {
            Types.Number
        }
        Boolean::class.java -> {
            Types.Boolean
        }
        else -> {
            when {
                List::class.java.isAssignableFrom(clazz) -> {
                    Types.Array
                }
                else -> {
                    Types.Object
                }
            }
        }
    }
    return type.name
}

fun getTypeString(jsonToken: JsonToken): String {
    val type = when (jsonToken) {
        JsonToken.STRING -> {
            Types.String
        }
        JsonToken.NUMBER -> {
            Types.Number
        }
        JsonToken.BOOLEAN -> {
            Types.Boolean
        }
        JsonToken.BEGIN_ARRAY -> {
            Types.Array
        }
        JsonToken.BEGIN_OBJECT -> {
            Types.Object
        }
        else -> {
            Types.Unknown
        }
    }
    return type.name
}

fun isSimpleType(clazz: Class<*>): Boolean {
    return simpleTypes.contains(clazz)
}

val simpleTypes: Set<Class<*>> = setOf(
        String::class.java,
        Boolean::class.java,
        Char::class.java,
        Byte::class.java,
        Short::class.java,
        Int::class.java,
        Long::class.java,
        Float::class.java,
        Double::class.java,
        Void::class.java
)
