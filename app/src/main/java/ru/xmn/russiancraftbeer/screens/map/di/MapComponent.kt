package ru.xmn.russiancraftbeer.screens.map.di

import com.vicpin.krealmextensions.queryAllAsFlowable
import com.vicpin.krealmextensions.save
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import io.reactivex.schedulers.Schedulers
import khronos.days
import ru.xmn.common.extensions.androidLogger
import ru.xmn.common.extensions.newerThan
import ru.xmn.common.repo.*
import ru.xmn.russiancraftbeer.commonBeerRepo
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.MapViewModel
import ru.xmn.russiancraftbeer.screens.map.ui.pubviewmodel.PubViewModel
import ru.xmn.russiancraftbeer.services.beer.BeerService
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubShortData
import ru.xmn.russiancraftbeer.services.beer.data.PubShortDataMapper
import ru.xmn.russiancraftbeer.services.beer.data.PubShortDataRealm

@Subcomponent(modules = [(MapModule::class)])
interface MapComponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): MapComponent
        fun mapModule(mapModule: MapModule): Builder
    }

    fun inject(mapViewModel: MapViewModel)
    fun inject(mapViewModel: PubViewModel)
}

@Module
class MapModule {
    @Provides
    fun provideMapListUseCase(beerService: BeerService): MapListUseCase {
        val repo: RepoSingleFactory<Unit, List<PubShortData>> = commonBeerRepo(
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