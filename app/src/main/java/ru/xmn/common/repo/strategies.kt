package ru.xmn.common.repo

import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import io.reactivex.Flowable
import io.reactivex.exceptions.CompositeException

typealias RepoEither<Value> = Either<Throwable, Value>

inline fun <Q, T> repoLocalThenNetwork(
        crossinline localMaybeStreamFactory: LocalStreamFactory<Q, T>,
        crossinline networkSingleStreamFactory: NetworkStreamFactory<Q, T>)
        : RepoFlowableFactory<Q, T> {
    return { query ->
        val localThenNetwork = listOf(localMaybeStreamFactory(query).toFlowable(), networkSingleStreamFactory(query).toFlowable())
        Flowable.concatDelayError(localThenNetwork)
                .map<RepoEither<T>> { Right(it) }
                .onErrorReturn { if (it is CompositeException) Left(RepoExceptions.from(it)) else Left(it) }
    }
}

inline fun <Q, T> repoNetwork(
        crossinline networkSingleStreamFactory: NetworkStreamFactory<Q, T>)
        : RepoSingleFactory<Q, T> {
    return { query ->
        networkSingleStreamFactory(query)
                .map<RepoEither<T>> { Right(it) }
                .onErrorReturn { Left(it) }
    }
}

inline fun <Q, T> repoLocal(
        crossinline localMaybeStreamFactory: LocalStreamFactory<Q, T>)
        : RepoMaybeFactory<Q, T> {
    return { query ->
        localMaybeStreamFactory(query)
                .map<RepoEither<T>> { Right(it) }
                .onErrorReturn { Left(it) }
    }
}