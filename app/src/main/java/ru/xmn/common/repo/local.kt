package ru.xmn.common.repo

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

typealias LocalStreamFactory<Q, Value> = (Q) -> Maybe<Value>

inline infix fun <Q, Value> LocalStreamFactory<Q, Value>.transformStream(crossinline then: Maybe<Value>.(Q) -> Maybe<Value>)
        : LocalStreamFactory<Q, Value> = { query -> this(query).then(query) }

inline infix fun <Q, Value> LocalStreamFactory<Q, Value>.locLog(crossinline logger: (String) -> Unit)
        : LocalStreamFactory<Q, Value> = transformStream { locLog(logger) }

inline infix fun <Q, Value> LocalStreamFactory<Q, Value>.validatedBy(crossinline predicate: (Value) -> Boolean)
        : LocalStreamFactory<Q, Value> = transformStream { validatedBy(predicate) }

const val LOCAL_SUCCESS = "local success"
const val LOCAL_ERROR = "local error"

inline infix fun <Value> Maybe<Value>.locLog(crossinline logger: (String) -> Unit)
        : Maybe<Value> =
        log(logger, beforeSuccess = LOCAL_SUCCESS, beforeError = LOCAL_ERROR)

inline infix fun <Value> Single<Value>.locLog(crossinline logger: (String) -> Unit)
        : Single<Value> =
        log(logger, beforeSuccess = LOCAL_SUCCESS, beforeError = LOCAL_ERROR)

inline infix fun <Value> Flowable<Value>.locLog(crossinline logger: (String) -> Unit)
        : Flowable<Value> =
        log(logger, beforeSuccess = LOCAL_SUCCESS, beforeError = LOCAL_ERROR)