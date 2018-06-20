package ru.xmn.common.extensions

fun <T> List<T>.printFirst(macCountForPrint: Int, printItem: (T) -> String = { it.toString() }) =
        "${map { "Pub(${printItem(it)})" }.take(macCountForPrint)}${if (size > macCountForPrint) "..." else ""}"