package ru.xmn.russiancraftbeer.application.di

import dagger.Component
import ru.xmn.russiancraftbeer.services.beer.BeerModule
import ru.xmn.russiancraftbeer.screens.map.di.MapComponent
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class, NetworkModule::class, BeerModule::class])
interface ApplicationComponent {
    var provideMapComponentBuilder: MapComponent.Builder
}

