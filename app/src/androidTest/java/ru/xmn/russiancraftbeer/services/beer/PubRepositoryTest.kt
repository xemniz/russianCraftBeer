package ru.xmn.russiancraftbeer.services.beer

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
        PubRealm().also {
            it.nid = "123"
            it.logo = ""
            it.body = ""
            it.address = ""
            it.map = ""
            it.phones = ""
            it.site = ""
            it.date = Dates.now - 2.days
        }.saveManaged(testRealm)

        val pub = pubRepository.getPub("123").blockingFirst()
        assertEquals("test", pub.nid)
    }

}