package ru.xmn.russiancraftbeer.services.beer

import io.reactivex.Single
import retrofit2.http.*
import ru.xmn.russiancraftbeer.services.beer.data.PubFullDataRealm
import ru.xmn.russiancraftbeer.services.beer.data.PubShortDataRealm

interface BeerService {
    @GET("json_data_3/json_data_23")
    fun getPubListMap(): Single<List<PubShortDataRealm>>

    @GET("json_data_3/nidinfo")
    fun getPub(@Query("nid") id: String): Single<List<PubFullDataRealm>>
}