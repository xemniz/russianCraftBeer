package ru.xmn.russiancraftbeer.services.beer.data

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import khronos.Dates
import java.util.*

open class PubFullDataRealm : RealmObject() {
    @PrimaryKey
    var nid: String? = null
    var logo: String? = null
    var body: String? = null
    var address: RealmList<String>? = null
    var map: RealmList<MapPointRealm>? = null
    var phones: RealmList<String>? = null
    var site: RealmList<String>? = null
    var date: Date = Dates.now
}

