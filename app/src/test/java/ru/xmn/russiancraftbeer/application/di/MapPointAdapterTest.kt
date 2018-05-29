package ru.xmn.russiancraftbeer.application.di

import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Test
import ru.xmn.russiancraftbeer.services.beer.data.MapPointRealm

/**
 * Created by xmn on 20.08.2017.
 */
class MapPointAdapterTest {
    @Test
    fun fromJson() {
        val testString = """"{\"type\":\"Point\",\"coordinates\":[37.6070175,55.7509607]}""""
        val mapPointAdapter = MapPointAdapter()
        val fromJson = GsonBuilder().registerTypeAdapter(MapPointRealm::class.java, mapPointAdapter).create().fromJson(testString, MapPointRealm::class.java)
        assertEquals(37.6070175, fromJson.coordinates[0]!!.toDouble(), 0.09)
    }

}