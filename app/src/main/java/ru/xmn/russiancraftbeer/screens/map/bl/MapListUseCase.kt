package ru.xmn.russiancraftbeer.screens.map.bl

import io.reactivex.Flowable
import ru.xmn.russiancraftbeer.screens.map.bl.data.MapPoint
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubShortData
import ru.xmn.common.repo.RepoEither
import ru.xmn.common.repo.RepoSingleFactoryNoParams


class MapListUseCase(private val repoFactory: RepoSingleFactoryNoParams<List<PubShortData>>) {
    fun getPubsForMap(locationForSort: MapPoint): Flowable<RepoEither<List<PubShortData>>> {
        return repoFactory()
                .map { either ->
                    either.map {
                        it.sortedWith(DistanceComparator(locationForSort))
                    }
                }.toFlowable()
    }
}
