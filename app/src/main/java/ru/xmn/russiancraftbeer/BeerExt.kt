package ru.xmn.russiancraftbeer

import io.reactivex.Single
import ru.xmn.common.repo.*

fun<Q, Raw, Mapped> commonBeerRepo(
        networkSingleFactory: (Q) -> Single<Raw>,
        localMaybeStreamFactory: LocalStreamFactory<Q, Raw>,
        cacheSaver: (Raw, Q) -> Unit,
        validator: (Raw) -> Boolean,
        logger: (String) -> Unit,
        mapper: (Raw) -> Mapped
): RepoSingleFactory<Q, Mapped> {
    val repoFlowable: RepoFlowableFactory<Q, Mapped> = repoLocalThenNetwork(
            networkSingleStreamFactory = networkSingleFactory
                    netwLog logger
                    cachedBy cacheSaver,
            localMaybeStreamFactory = localMaybeStreamFactory
                    validatedBy validator
                    locLog logger
    ) mapValueWith mapper

    return repoFlowable.firstSuccessOrError()
}