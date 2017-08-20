package ru.xmn.russiancraftbeer.application.di

import android.content.Context
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import ru.xmn.common.extensions.fromJson
import ru.xmn.russiancraftbeer.services.beer.MapPoint
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {
    @Provides @Singleton
    fun provideCache(context: Context)
            = Cache(context.cacheDir, 10 * 1024 * 1024.toLong())

    @Provides @Singleton
    fun provideOkHttpClient(cache: Cache): OkHttpClient
            = OkHttpClient()
            .newBuilder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .cache(cache)
            .build()

    @Provides @Singleton
    fun moshi(): Moshi = Moshi.Builder().add(MapPointAdapter()).build()
}

class MapPointAdapter {
    @FromJson
    fun fromJson(s: String): MapPoint{
        val result: String = s.replace("\\\"", "\"").replace("\"{", "{").replace("\"}", "}")
        return Moshi.Builder().build().fromJson<MapPoint>(result)!!
    }
}


fun provideRestAdapter(client: OkHttpClient, url: String, moshi: Moshi): Retrofit
        = Retrofit.Builder()
        .baseUrl(url)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

fun OkHttpClient.addParameterInterceptor(key: String, value: String): OkHttpClient {
    return this.newBuilder()
            .addInterceptor {
                val url = it.request().url().newBuilder().addQueryParameter(key, value).build()
                val request = it.request().newBuilder().url(url).build()
                it.proceed(request)
            }
            .build()
}