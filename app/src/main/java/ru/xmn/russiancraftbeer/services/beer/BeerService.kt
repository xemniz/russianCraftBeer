package ru.xmn.russiancraftbeer.services.beer

import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.http.*

interface BeerService {
    @GET("json_data_3/json_data_23")
    fun getPubListMap(): Flowable<List<PubMapDto>>

    @GET("json_data_3/nidinfo")
    fun getPub(@Query("nid") id: String): Flowable<PubDto>
}