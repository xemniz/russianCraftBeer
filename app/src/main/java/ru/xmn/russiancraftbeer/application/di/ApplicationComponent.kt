package ru.xmn.russiancraftbeer.application.di

import dagger.Component
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.MapViewModel
import ru.xmn.russiancraftbeer.screens.map.ui.pubviewmodel.PubViewModel
import ru.xmn.russiancraftbeer.services.beer.BeerModule
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, NetworkModule::class, BeerModule::class, MapModule::class])
interface ApplicationComponent {
    fun inject(mapViewModel: MapViewModel)
    fun inject(mapViewModel: PubViewModel)
}

