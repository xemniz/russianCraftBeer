package ru.xmn.common.repo

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

fun String.beforeWhiteSpaceIfNotBlank() = if (isNotBlank()) " ${this}" else this
fun String.afterWhiteSpaceIfNotBlank() = if (isNotBlank()) "${this} " else this
fun <Value> makeLogString(beforeSuccess: String, it: Value?, afterSuccess: String) =
        "${beforeSuccess.afterWhiteSpaceIfNotBlank()}$it${afterSuccess.beforeWhiteSpaceIfNotBlank()}"

inline fun <Value> Maybe<Value>.log(
        crossinline logger: (String) -> Unit,
        beforeError: String = "",
        afterError: String = "",
        beforeSuccess: String = "",
        afterSuccess: String = "")
        : Maybe<Value> = doOnSuccess { logger(makeLogString(beforeSuccess, it, afterSuccess)) }
        .doOnError {
            it.printStackTrace()
            logger(makeLogString(beforeError, it, afterError))
        }

inline fun <Value> Single<Value>.log(
        crossinline logger: (String) -> Unit,
        beforeError: String = "",
        afterError: String = "",
        beforeSuccess: String = "",
        afterSuccess: String = "")
        : Single<Value> = doOnSuccess { logger(makeLogString(beforeSuccess, it, afterSuccess)) }
        .doOnError {
            it.printStackTrace()
            logger(makeLogString(beforeError, it, afterError))
        }

inline fun <Value> Flowable<Value>.log(
        crossinline logger: (String) -> Unit,
        beforeError: String = "",
        afterError: String = "",
        beforeSuccess: String = "",
        afterSuccess: String = "")
        : Flowable<Value> =
        doOnNext { logger(makeLogString(beforeSuccess, it, afterSuccess)) }
                .doOnError {
                    it.printStackTrace()
                    logger(makeLogString(beforeError, it, afterError))
                }


inline infix fun <Value> Maybe<Value>.validatedBy(crossinline predicate: (Value) -> Boolean)
        : Maybe<Value> = filter { predicate(it) }

inline infix fun <Value> Single<Value>.validatedBy(crossinline predicate: (Value) -> Boolean)
        : Maybe<Value> = filter { predicate(it) }

inline infix fun <Value> Flowable<Value>.validatedBy(crossinline predicate: (Value) -> Boolean)
        : Flowable<Value> = filter { predicate(it) }

inline fun <Value, Param> Maybe<Value>.cachedBy(crossinline saveInCache: (Value, Param) -> Unit, query: Param)
        : Maybe<Value> = doOnSuccess { saveInCache(it, query) }

inline fun <Value, Param> Single<Value>.cachedBy(crossinline saveInCache: (Value, Param) -> Unit, query: Param)
        : Single<Value> = doOnSuccess { saveInCache(it, query) }

inline fun <Value, Param> Flowable<Value>.cachedBy(crossinline saveInCache: (Value, Param) -> Unit, query: Param)
        : Flowable<Value> = doOnNext { saveInCache(it, query) }