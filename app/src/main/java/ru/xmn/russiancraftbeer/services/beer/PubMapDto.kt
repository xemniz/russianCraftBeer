package ru.xmn.russiancraftbeer.services.beer

import com.squareup.moshi.Moshi
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import khronos.Dates
import ru.xmn.common.extensions.deserialize
import ru.xmn.common.extensions.fromJson
import ru.xmn.common.extensions.serialize
import ru.xmn.common.extensions.toJson
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
open class PubMapRealm() : RealmObject() {

    var map: String? = null
    var address: String? = null
    @PrimaryKey var nid: String? = null
    var type: String? = null
    var title: String? = null
    var field_logo: String? = null
    var date: Date? = null
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