@file:Suppress("UNCHECKED_CAST")

package ru.xmn.common.extensions

import io.reactivex.observers.BaseTestConsumer
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue

fun <T> BaseTestConsumer<T, *>.assertResults(expectedStates: List<T>, errorsCount: Int = 0, completesCount: Int = 1) {
    val (successList, errorList, completeList) = (this as BaseTestConsumer<T, Nothing>).events
    assertEquals(expectedStates, successList)
    assertEquals(errorsCount, errorList.size)
    assertEquals(completesCount, completeList.size)
}

fun <T> BaseTestConsumer<T, *>.assertDsl(assertions: BaseTestConsumer<T, *>.() -> Unit) {
    assertions()
}

fun <T> BaseTestConsumer<T, *>.assertErrorsCount(expectedCount: Int) {
    assertCountEquals(expectedCount, { errorCount() })
}

fun <T> BaseTestConsumer<T, *>.assertCompletionsCount(expectedCount: Int) {
    assertCountEquals(expectedCount, { (this as BaseTestConsumer<T, Nothing>).events[2].size })
}

fun <T> BaseTestConsumer<T, *>.assertOnNextCount(expectedCount: Int) {
    assertCountEquals(expectedCount, { (this as BaseTestConsumer<T, Nothing>).events[0].size })
}

fun <T> BaseTestConsumer<T, *>.assertErrorsCount(predicate: (Int) -> Boolean) {
    assertCountEquals(predicate, { errorCount() })
}

fun <T> BaseTestConsumer<T, *>.assertCompletionsCount(predicate: (Int) -> Boolean) {
    assertCountEquals(predicate, { (this as BaseTestConsumer<T, Nothing>).events[2].size })
}

fun <T> BaseTestConsumer<T, *>.assertOnNextCount(predicate: (Int) -> Boolean) {
    assertCountEquals(predicate, { (this as BaseTestConsumer<T, Nothing>).events[0].size })
}

fun <T> BaseTestConsumer<T, *>.assertCountEquals(expectedCount: Int, countProvider: BaseTestConsumer<T, *>.() -> Int) {
    assertEquals(expectedCount, countProvider())
}

fun <T> BaseTestConsumer<T, *>.assertCountEquals(predicate: (Int) -> Boolean, countProvider: BaseTestConsumer<T, *>.() -> Int) {
    assertTrue(predicate(countProvider()))
}