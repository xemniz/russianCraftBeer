package ru.xmn.russiancraftbeer.services.beer

import com.squareup.moshi.Moshi
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import khronos.Dates
import ru.xmn.common.extensions.*
import java.util.*

data class PubDto(
        val logo: String?,
        val body: String?,
        val address: List<String>?,
        val map: List<MapPoint>?,
        val phones: List<String>?,
        val site: List<String>?
){
    var nid: String? = null

    companion object {
        fun empty() = PubDto("", "", emptyList(), emptyList(), emptyList(), emptyList())
    }
}

open class PubRealm() : RealmObject() {
    var logo: String? = null
    var body: String? = null
    var address: String? = null
    var map: String? = null
    var phones: String? = null
    var site: String? = null
    var date: Date? = null
    @PrimaryKey var nid: String? = null
}

fun PubDto.toRealm() = PubRealm().apply {
    logo = this@toRealm.logo
    body = this@toRealm.body
    address = this@toRealm.address.serialize()
    map = this@toRealm.map?.map { it.toJson() }.serialize()
    phones = this@toRealm.phones.serialize()
    site = this@toRealm.site.serialize()
    date = Dates.now
    nid = this@toRealm.nid
}

fun PubRealm.fromRealm() = PubDto(
        logo,
        body,
        address.deserialize(),
        map.deserialize().map { Moshi.Builder().build().fromJson<MapPoint>(it)!! },
        phones.deserialize(),
        site.deserialize()
).apply { nid = this@fromRealm.nid }