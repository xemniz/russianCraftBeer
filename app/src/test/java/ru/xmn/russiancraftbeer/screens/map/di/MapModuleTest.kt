package ru.xmn.russiancraftbeer.screens.map.di

import okhttp3.OkHttpClient
import org.junit.Test
import ru.xmn.common.extensions.assertDsl
import ru.xmn.common.extensions.assertErrorsCount
import ru.xmn.common.extensions.assertOnNextCount
import ru.xmn.russiancraftbeer.application.di.NetworkModule
import ru.xmn.russiancraftbeer.application.di.provideRestAdapter
import ru.xmn.russiancraftbeer.services.beer.BeerService

class MapModuleTest {

    @Test
    fun testBeerLoading() {
        val provideRestAdapter = provideRestAdapter(OkHttpClient(), "http://russiancraftbeer.ru/", NetworkModule().gson())
        provideRestAdapter.create(BeerService::class.java).getPubListMap().test().assertDsl {
            assertErrorsCount(0)
            assertOnNextCount { it > 0 }
        }
    }
}

