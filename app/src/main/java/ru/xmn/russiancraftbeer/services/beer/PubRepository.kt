package ru.xmn.russiancraftbeer.services.beer

import com.vicpin.krealmextensions.query
import com.vicpin.krealmextensions.queryAllAsFlowable
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import khronos.Dates
import khronos.days
import khronos.hours
import khronos.minus
import ru.xmn.common.extensions.stripHtml

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
        val query = PubRealm().query { query -> query.equalTo("nid", id) }
        if (query.isEmpty() || Dates.now - 1.hours > query[0].date)
            return getPubFromNetwork(id)
        else
            return Flowable.just(query[0].fromRealm())
    }

    private fun getPubFromNetwork(id: String): Flowable<PubDto> {
        return service.getPub(id)
                .map { it[0] }
                .map { it.also { it.nid = id } }
                .map {
                    it.copy(
                            logo = it.logo!!.substringBefore("\" width").substringAfter("src=\""),
                            body = it.body?.stripHtml()
                    )
                }
                .doOnNext { it.toRealm().save() }
                .subscribeOn(Schedulers.io())
    }

    private fun getPubsFromNetwork(): Flowable<List<PubMapDto>> {
        return service.getPubListMap()
                .doOnNext({
                    it.map {
                        it.copy(
                                field_logo = it.field_logo!!.substringBefore("\" width").substringAfter("src=\""),
                                title = it.title?.stripHtml()
                        )
                    }.map { it.toRealm() }.saveAll()
                }
                )
                .subscribeOn(Schedulers.io())
    }
}
