package ru.xmn.common.repo

import arrow.core.Either
import arrow.core.Left
import io.reactivex.Flowable

typealias RepoFlowableFactory<Q, Value> = (Q) -> Flowable<RepoEither<Value>>
typealias RepoFlowableFactoryNoParams<Value> = () -> Flowable<RepoEither<Value>>

inline infix fun <Q, Value, OutValue> RepoFlowableFactory<Q, Value>.transformStream(crossinline then: Flowable<RepoEither<Value>>.() -> Flowable<RepoEither<OutValue>>)
        : RepoFlowableFactory<Q, OutValue> = { query -> this(query).then() }

inline infix fun <Q, Value, MappedValue> RepoFlowableFactory<Q, Value>.mapValueWith(crossinline mapper: (Value) -> MappedValue) =
        transformStream {
            map { either -> either.map { value -> mapper(value) } }
                    .onErrorReturn { Left(it) }
        }

fun <Q, Value> RepoFlowableFactory<Q, Value>.noErrorsAfterSuccess(): RepoFlowableFactory<Q, Value> =
        transformStream {
            scan { oldState: RepoEither<Value>, newState: RepoEither<Value> ->
                when {
                    oldState is Either.Right && newState is Either.Left -> oldState
                    else -> newState
                }
            }
                    .distinct()
        }

fun <Value> RepoFlowableFactory<Unit, Value>.withoutParams(): RepoFlowableFactoryNoParams<Value> = { this(Unit) }

fun <Q, Value> RepoFlowableFactory<Q, Value>.firstSuccessOrError(): RepoSingleFactory<Q, Value> = { query: Q ->
    this(query).first(Left(IllegalStateException("No values on upstream")))
}