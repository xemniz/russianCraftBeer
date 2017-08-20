package ru.xmn.russiancraftbeer.services.beer

import com.vicpin.krealmextensions.query
import com.vicpin.krealmextensions.queryAllAsFlowable
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import khronos.Dates
import khronos.days
import khronos.hours
import khronos.minus

class PubRepository(val service: BeerService) {
    fun getPubListMap(): Flowable<List<PubMapDto>> {
        return PubMapRealm().queryAllAsFlowable()
                .flatMap {
                    if (it.isEmpty() || Dates.now - 1.days > it[0].date)
                        getPubsFromNetwork()
                    else
                        Flowable.just(it.map { it.fromRealm() })
                }
    }

    fun getPub(id: String): Flowable<PubDto> {
        val query = PubRealm().query { query -> query.equalTo("id", id) }
        if (query.isEmpty() || Dates.now - 1.hours > query[0].date)
            return service.getPub(id)
                    .map { it.also { it.nid = id } }
                    .doOnNext { it.toRealm().save() }
                    .subscribeOn(Schedulers.io())
        else
            return Flowable.just(query[0].fromRealm())
    }

    private fun getPubsFromNetwork(): Flowable<List<PubMapDto>> {
        return service.getPubListMap()
                .doOnNext({ it.map { it.toRealm() }.saveAll() })
                .subscribeOn(Schedulers.io())
    }
}
