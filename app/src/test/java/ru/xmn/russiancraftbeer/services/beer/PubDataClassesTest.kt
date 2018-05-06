package ru.xmn.russiancraftbeer.services.beer

import org.junit.Assert.*
import org.junit.Test

/**
 * Created by xmn on 18.08.2017.
 */
class PubDataClassesTest {
    @Test
    fun fromPubDtoRealmToRealmTest() {
        val pubDto = PubDto(
                "logo",
                "body",
                listOf("one", "two", "three"),
                listOf(MapPoint("x", listOf(12.toDouble(),12.toDouble()))),
                listOf("one", "two", "three"),
                listOf("one", "two", "three")
        ).apply { nid = "123123" }
        val toRealm = pubDto.toRealm()
        assertEquals(pubDto, toRealm.fromRealm())
    }

    @Test
    fun fromPubMapDtoRealmToRealmTest() {
        val pubDto = PubMapDto(
                listOf(MapPoint("one", listOf(1.1, 2.2)), MapPoint("one", listOf(1.1, 2.2)), MapPoint("one", listOf(1.1, 2.2))),
                listOf("one", "two", "three"),
                "logo",
                "body",
                "logo",
                "body"
        )
        val toRealm = pubDto.toRealm()
        assertEquals(pubDto, toRealm.fromRealm())
    }
}