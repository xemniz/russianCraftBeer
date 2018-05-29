package ru.xmn.common.repo

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

typealias NetworkStreamFactory<Q, Value> = (Q) -> Single<Value>

inline infix fun <Q, Value> NetworkStreamFactory<Q, Value>.transformStream(crossinline then: Single<Value>.(Q) -> Single<Value>)
        : NetworkStreamFactory<Q, Value> = { query -> this(query).then(query) }

inline infix fun <Q, Value> NetworkStreamFactory<Q, Value>.cachedBy(crossinline saveInCache: (Value, Q) -> Unit)
        : NetworkStreamFactory<Q, Value> = transformStream { query -> cachedBy(saveInCache, query) }

inline infix fun <Q, Value> NetworkStreamFactory<Q, Value>.netwLog(crossinline logger: (String) -> Unit)
        : NetworkStreamFactory<Q, Value> = transformStream { netwLog(logger) }

const val NETWORK_SUCCESS = "network success"
const val NETWORK_ERROR = "network error"

inline infix fun <Value> Maybe<Value>.netwLog(crossinline logger: (String) -> Unit)
        : Maybe<Value> =
        log(logger, beforeSuccess = NETWORK_SUCCESS, afterSuccess = NETWORK_ERROR)

inline infix fun <Value> Single<Value>.netwLog(crossinline logger: (String) -> Unit)
        : Single<Value> =
        log(logger, beforeSuccess = NETWORK_SUCCESS, afterSuccess = NETWORK_ERROR)

inline infix fun <Value> Flowable<Value>.netwLog(crossinline logger: (String) -> Unit)
        : Flowable<Value> =
        log(logger, beforeSuccess = NETWORK_SUCCESS, afterSuccess = NETWORK_ERROR)