package ru.xmn.russiancraftbeer.application.di

import android.content.Context
import com.google.gson.*
import dagger.Module
import dagger.Provides
import io.realm.RealmObject
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ru.xmn.common.extensions.fromJson
import ru.xmn.russiancraftbeer.services.beer.data.MapPointRealm
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {
    @Provides
    @Singleton
    fun provideCache(context: Context) = Cache(context.cacheDir, 10 * 1024 * 1024.toLong())

    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache): OkHttpClient = OkHttpClient()
            .newBuilder()
            .connectTimeout(5, TimeUnit.MINUTES)
            .writeTimeout(5, TimeUnit.MINUTES)
            .readTimeout(5, TimeUnit.MINUTES)
            .cache(cache)
            .build()

    @Provides
    @Singleton
    fun gson(): Gson = GsonBuilder()
            .setExclusionStrategies(object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>?) = false
                override fun shouldSkipField(f: FieldAttributes) = f.declaringClass == RealmObject::class.java
            }).registerTypeAdapter(MapPointRealm::class.java, MapPointAdapter()).create()
}

//todo в пивной модуль
class MapPointAdapter : JsonDeserializer<MapPointRealm> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): MapPointRealm? {
        return (json?.asString ?: "").replaceGarbageForMapPointResponse().fromJson()
    }
}

fun String.replaceGarbageForMapPointResponse() = replace("\\\"", "\"").replace("\"{", "{").replace("\"}", "}")

fun provideRestAdapter(client: OkHttpClient, url: String, moshi: Gson): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(moshi))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()