package ru.xmn.russiancraftbeer.services.beer

import com.google.android.gms.maps.model.LatLng
import com.vicpin.krealmextensions.saveManaged
import io.reactivex.Flowable
import io.realm.Realm
import org.junit.After
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import io.realm.RealmConfiguration
import khronos.Dates
import khronos.days
import khronos.hour
import khronos.minus


/**
 * Created by xmn on 19.08.2017.
 */
class PubRepositoryTest {
    lateinit var testRealm: Realm
    lateinit var pubRepository: PubRepository
    val beerService: BeerService = object : BeerService {
        override fun getPubListMap(): Flowable<List<PubMapDto>> {
            return Flowable.empty<List<PubMapDto>>()
        }

        override fun getPub(id: String): Flowable<List<PubDto>> {
            return Flowable.just(
                    listOf(PubDto(
                            "",
                            "",
                            emptyList(),
                            emptyList(),
                            emptyList(),
                            emptyList()
                    ).apply { nid = "test" }
                    ))
        }
    }

    @Before
    fun setUp() {
        val testConfig = RealmConfiguration.Builder().inMemory().name("test-realm").build()
        Realm.setDefaultConfiguration(testConfig)
        testRealm = Realm.getInstance(testConfig)
        pubRepository = PubRepository(beerService)
        assertEquals(testRealm, Realm.getDefaultInstance())
    }

    @After
    fun tearDown() {
        testRealm.close()
    }

    @Test
    fun getPubListMap() {
    }

    @Test
    fun getPub() {
        PubDto("logo", "B", listOf(",","s"), listOf(MapPoint("sd", listOf(2.0,2.4))), listOf("\"sdf\""), listOf(" ")).also { it.nid = "123" }.toRealm().saveManaged(testRealm)

        val pub = pubRepository.getPub("123").blockingFirst()
        assertEquals("123", pub.nid)
    }

}