package ru.xmn.russiancraftbeer.screens.map.di

import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import ru.xmn.russiancraftbeer.services.beer.PubRepository
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import ru.xmn.russiancraftbeer.screens.map.bl.PubUseCase
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.MapViewModel
import ru.xmn.russiancraftbeer.screens.map.ui.pubviewmodel.PubViewModel

@Subcomponent(modules = arrayOf(MapModule::class))
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
    fun provideMapListUseCase(repository: PubRepository) = MapListUseCase(repository)
    @Provides
    fun pubUseCase(repository: PubRepository) = PubUseCase(repository)
}