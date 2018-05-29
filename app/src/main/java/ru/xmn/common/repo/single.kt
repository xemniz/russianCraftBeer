package ru.xmn.common.repo

import io.reactivex.Single

typealias RepoSingleFactory<Q, Value> = (Q) -> Single<RepoEither<Value>>
typealias RepoSingleFactoryNoParams<Value> = () -> Single<RepoEither<Value>>

fun <Value> RepoSingleFactory<Unit, Value>.withoutParamsSingle(): RepoSingleFactoryNoParams<Value> = { this(Unit) }