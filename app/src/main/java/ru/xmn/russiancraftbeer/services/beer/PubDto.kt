package ru.xmn.russiancraftbeer.services.beer

import com.squareup.moshi.Moshi
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import khronos.Dates
import ru.xmn.common.extensions.*
import java.util.*

data class PubMapDto(
        val map: List<MapPoint>?,
        val address: List<String>?,
        val nid: String?,
        val type: String?,
        val title: String?,
        val field_logo: String?
)

data class MapPoint(val type: String, val coordinates: List<Double>)

data class PubDto(
        val logo: String?,
        val body: String?,
        val address: List<String>?,
        val map: List<String>?,
        val phones: List<String>?,
        val site: List<String>?
){
    var nid: String? = null
}

open class PubMapRealm() : RealmObject() {
    var map: String? = null
    var address: String? = null
    @PrimaryKey var nid: String? = null
    var type: String? = null
    var title: String? = null
    var field_logo: String? = null
    var date: Date? = null
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

fun PubMapDto.toRealm() = PubMapRealm().apply {
    map = this@toRealm.map?.map { it.toJson() }.serialize()
    address = this@toRealm.address.serialize()
    nid = this@toRealm.nid
    type = this@toRealm.type
    title = this@toRealm.title
    field_logo = this@toRealm.field_logo
    date = Dates.now
}

fun PubMapRealm.fromRealm() = PubMapDto(
        map.deserialize().map { Moshi.Builder().build().fromJson<MapPoint>(it)!! },
        address.deserialize(),
        nid,
        type,
        title,
        field_logo
)

fun PubDto.toRealm() = PubRealm().apply {
    logo = this@toRealm.logo
    body = this@toRealm.body
    address = this@toRealm.address.serialize()
    map = this@toRealm.map.serialize()
    phones = this@toRealm.phones.serialize()
    site = this@toRealm.site.serialize()
    date = Dates.now
    nid = this@toRealm.nid
}

fun PubRealm.fromRealm() = PubDto(
        logo,
        body,
        address.deserialize(),
        map.deserialize(),
        phones.deserialize(),
        site.deserialize()
).apply { nid = this@fromRealm.nid }