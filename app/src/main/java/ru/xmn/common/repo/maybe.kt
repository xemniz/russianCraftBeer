package ru.xmn.common.repo

import io.reactivex.Maybe

typealias RepoMaybeFactory<Q, Value> = (Q) -> Maybe<RepoEither<Value>>
typealias RepoMaybeFactoryNoParams<Value> = () -> Maybe<RepoEither<Value>>

fun <Value> RepoMaybeFactory<Unit, Value>.withoutParamsMaybe(): RepoMaybeFactoryNoParams<Value> = { this(Unit) }