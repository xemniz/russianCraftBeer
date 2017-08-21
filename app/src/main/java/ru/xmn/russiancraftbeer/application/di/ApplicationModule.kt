package ru.xmn.russiancraftbeer.application.di

import android.content.Context
import dagger.Module
import dagger.Provides
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.di.MapComponent
import javax.inject.Singleton

@Module(subcomponents = arrayOf(MapComponent::class))
class ApplicationModule(private val app: App) {
    @Provides @Singleton
    fun provideApplicationContext(): Context = app
}