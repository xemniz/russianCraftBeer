package ru.xmn.russiancraftbeer.screens.map.di

import com.vicpin.krealmextensions.queryAllAsFlowable
import com.vicpin.krealmextensions.save
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import khronos.days
import ru.xmn.common.extensions.androidLogger
import ru.xmn.common.extensions.newerThan
import ru.xmn.common.repo.*
import ru.xmn.russiancraftbeer.commonBeerRepo
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import ru.xmn.russiancraftbeer.services.beer.BeerService
import ru.xmn.russiancraftbeer.screens.map.bl.data.Pubs
import ru.xmn.russiancraftbeer.services.beer.data.PubShortDataMapper
import ru.xmn.russiancraftbeer.services.beer.data.PubShortDataRealm

@Module
class MapModule {
    @Provides
    fun provideMapListUseCase(beerService: BeerService): MapListUseCase {
        val repo: RepoSingleFactory<Unit, Pubs> = commonBeerRepo(
                networkSingleFactory = {
                    beerService.getPubListMap()
                            .subscribeOn(Schedulers.io())
                },
                localMaybeStreamFactory = {
                    queryAllAsFlowable<PubShortDataRealm>()
                            .firstElement()
                            .filter { it.isNotEmpty() }
                            .subscribeOn(Schedulers.io())
                },
                cacheSaver = { raw, _ -> raw.forEach { it.save() } },
                validator = { it[0].date newerThan 1.days },
                logger = androidLogger(tag = "MapList"),
                mapper = PubShortDataMapper()::map
        )

        return MapListUseCase(repo.withoutParamsSingle())
    }
}