package ru.xmn.russiancraftbeer.services.beer

import com.google.gson.Gson
import com.vicpin.krealmextensions.queryAsFlowable
import com.vicpin.krealmextensions.save
import dagger.Module
import dagger.Provides
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import khronos.days
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import ru.xmn.common.extensions.androidLogger
import ru.xmn.common.extensions.newerThan
import ru.xmn.russiancraftbeer.application.di.provideRestAdapter
import ru.xmn.russiancraftbeer.commonBeerRepo
import ru.xmn.russiancraftbeer.screens.map.bl.PubFullDataQuery
import ru.xmn.russiancraftbeer.screens.map.ui.pubviewmodel.PubFullDataLiveData
import ru.xmn.common.repo.RepoSingleFactory
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubFullData
import ru.xmn.russiancraftbeer.services.beer.data.PubFullDataMapper
import ru.xmn.russiancraftbeer.services.beer.data.PubFullDataRealm
import javax.inject.Named
import javax.inject.Singleton

@Module
class BeerModule {
    companion object {
        const val NAME = "beer"
    }

    @Provides
    @Singleton
    @Named(NAME)
    fun provideRestAdapterBeer(client: OkHttpClient, gson: Gson): Retrofit = provideRestAdapter(client, "http://russiancraftbeer.ru/", gson)

    @Provides
    @Singleton
    fun providesBeerService(@Named(NAME) retrofit: Retrofit): BeerService = retrofit.create(BeerService::class.java)

    @Provides
    fun providesPubFullDataRepository(beerService: BeerService): PubFullDataLiveData {

        val repo: RepoSingleFactory<PubFullDataQuery, PubFullData> = commonBeerRepo(
                networkSingleFactory = { query ->
                    beerService.getPub(query.id)
                            .map { it.first() }
                            .subscribeOn(Schedulers.io())
                },
                localMaybeStreamFactory = { query ->
                    queryAsFlowable<PubFullDataRealm> { equalTo("nid", query.id) }
                            .firstElement()
                            .flatMap {
                                when {
                                    it.isNotEmpty() -> Maybe.just(it[0])
                                    else -> Maybe.empty()
                                }
                            }
                            .subscribeOn(Schedulers.io())
                },
                cacheSaver = { raw, query -> raw.apply { nid = query.id }.save() },
                validator = { it.date newerThan 1.days },
                logger = androidLogger(tag = "PubFullData"),
                mapper = PubFullDataMapper()::map
        )
        return PubFullDataLiveData(repo)
    }
}
