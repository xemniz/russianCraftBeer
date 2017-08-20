package ru.xmn.common.extensions

import org.junit.Assert.*
import org.junit.Test

class realmExtTest{
    @Test
    fun testListSerializer(){
        val list = listOf("one", "two", "three")
        val serialize = list.serialize()
        val deserialize = serialize.deserialize()
        assertEquals(list, deserialize)
    }
}