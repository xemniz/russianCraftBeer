package ru.xmn.russiancraftbeer.services.beer

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import ru.xmn.russiancraftbeer.application.di.provideRestAdapter
import javax.inject.Named
import javax.inject.Singleton

@Module
class BeerModule {
    companion object {
        const val NAME = "beer"
    }

    @Provides @Singleton @Named(NAME)
    fun provideRestAdapterBeer(client: OkHttpClient, moshi: Moshi): Retrofit
            = provideRestAdapter(client, "http://russiancraftbeer.ru/", moshi)

    @Provides @Singleton
    fun providesBeerService(@Named(NAME) retrofit: Retrofit): BeerService
            = retrofit.create(BeerService::class.java)

    @Provides @Singleton
    fun providesPubRepository(beerService: BeerService): PubRepository
            = PubRepository(beerService)
}
