package ru.xmn.russiancraftbeer.screens.map.bl

import io.reactivex.Flowable
import ru.xmn.common.repo.RepoEither
import ru.xmn.common.repo.RepoSingleFactoryNoParams
import ru.xmn.russiancraftbeer.screens.map.bl.data.Pubs


class MapListUseCase(private val repoFactory: RepoSingleFactoryNoParams<Pubs>) {
    fun getPubsForMap(): Flowable<RepoEither<Pubs>> {
        return repoFactory().toFlowable()
    }
}
