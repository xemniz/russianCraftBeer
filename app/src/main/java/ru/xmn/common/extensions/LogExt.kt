package ru.xmn.common.extensions

import android.util.Log


fun androidLogger(tag: String, msg: String? = null): (String) -> Unit {
    val msgText = msg?.let { "msg = $it, " } ?: ""
    return { Log.d(tag, msgText + it) }
}