package ru.xmn.common.repo

import arrow.core.Left
import arrow.core.Right
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.junit.Test

import org.junit.Assert.*
import ru.xmn.common.extensions.assertCompletionsCount
import ru.xmn.common.extensions.assertDsl
import ru.xmn.common.extensions.assertErrorsCount
import ru.xmn.common.extensions.assertResults

class LocalThenNetworkTest {
    private val local = Any()
    private val network = Any()
    private val localError: IllegalArgumentException = IllegalArgumentException()
    private val networkError: IllegalAccessError = IllegalAccessError()

    @Test
    fun whenHasLocalAndNetwork() {
        testRepository(
                localStream = Maybe.just(local),
                networkStream = Single.just(network),
                expectedStates = listOf(
                        Right(local),
                        Right(network)
                )
        )
    }

    @Test
    fun whenNotHasLocalAndNetwork() {
        testRepository(
                localStream = Maybe.empty(),
                networkStream = Single.just(network),
                expectedStates = listOf(
                        Right(network)
                )
        )
    }

    @Test
    fun whenHasLocalAndNetworkError() {
        testRepository(
                localStream = Maybe.just(local),
                networkStream = Single.error(networkError),
                expectedStates = listOf(
                        Right(local),
                        Left((networkError))
                )
        )
    }

    @Test
    fun whenNotHasLocalAndNetworkError() {
        testRepository(
                localStream = Maybe.empty(),
                networkStream = Single.error(networkError),
                expectedStates = listOf(
                        Left(networkError)
                ))
    }

    @Test
    fun whenNotHasLocalAndNetworkSuccess() {
        testRepository(
                localStream = Maybe.empty(),
                networkStream = Single.just(network),
                expectedStates = listOf(
                        Right(network)
                ))
    }

    @Test
    fun whenLocalErrorAndNetworkError() {
        testRepository(
                localStream = Maybe.error(localError),
                networkStream = Single.error(networkError),
                expectedStates = listOf(
                        Left(RepoExceptions.from(listOf(localError, networkError)))
                ))
    }

    @Test
    fun whenLocalErrorAndNetworkSuccess() {
        testRepository(
                localStream = Maybe.error(localError),
                networkStream = Single.just(network),
                expectedStates = listOf(
                        Right(network),
                        Left(localError)
                ))
    }

    private fun testRepository(localStream: Maybe<Any>, networkStream: Single<Any>, expectedStates: List<RepoEither<Any>>) {
        val flowable: Flowable<RepoEither<Any>> = repoLocalThenNetwork<Any, Any>({ localStream }, { networkStream })(Any())

        flowable.test().assertDsl {
            assertResults(expectedStates)
            assertCompletionsCount(1)
            assertErrorsCount(0)
        }
    }
}