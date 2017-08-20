package ru.xmn.russiancraftbeer.application.di

import org.junit.Test

import org.junit.Assert.*

/**
 * Created by xmn on 20.08.2017.
 */
class MapPointAdapterTest {
    @Test
    fun fromJson() {
        val testString = """"{\"type\":\"Point\",\"coordinates\":[37.6070175,55.7509607]}""""
        val mapPointAdapter = MapPointAdapter()
        val fromJson = mapPointAdapter.fromJson(testString)
        assertEquals(37.6070175, fromJson.coordinates[0], 0.09)
    }

}