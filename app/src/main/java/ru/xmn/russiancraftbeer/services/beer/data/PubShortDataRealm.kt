package ru.xmn.russiancraftbeer.services.beer.data

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import khronos.Dates
import java.util.*

open class PubShortDataRealm : RealmObject() {
    @PrimaryKey
    var nid: String? = null
    var map: RealmList<MapPointRealm>? = null
    var address: RealmList<String>? = null
    var type: String? = null
    var title: String? = null
    var field_logo: String? = null
    var date: Date = Dates.now
}

fun PubShortDataRealm.copy(
        nid: String? = null,
        map: RealmList<MapPointRealm>? = null,
        address: RealmList<String>? = null,
        type: String? = null,
        title: String? = null,
        field_logo: String? = null,
        date: Date? = null): PubShortDataRealm {
    val other = this
    return PubShortDataRealm().also { newPub ->
        newPub.nid = nid ?: other.nid
        newPub.map = map ?: other.map
        newPub.address = address ?: other.address
        newPub.type = type ?: other.type
        newPub.title = title ?: other.title
        newPub.field_logo = field_logo ?: other.field_logo
        newPub.date = date ?: other.date
    }
}