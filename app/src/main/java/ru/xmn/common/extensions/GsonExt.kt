package ru.xmn.common.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

val commonGson = Gson()

inline fun <reified T> String.fromJson(): T {
    return commonGson.fromJson(this, T::class.java)
}

inline fun <reified T> String.listFromJson(): T {
    val type = TypeToken.getParameterized(List::class.java, T::class.java).type
    return commonGson.fromJson(this, type)
}

inline fun <reified T> T.toJson(): String {
    return commonGson.toJson(this)
}