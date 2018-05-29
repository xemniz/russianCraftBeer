package ru.xmn.russiancraftbeer.services.beer.data

import io.realm.RealmList
import io.realm.RealmObject
import ru.xmn.russiancraftbeer.screens.map.bl.data.MapPoint

open class MapPointRealm : RealmObject() {
    var type: String = ""
    var coordinates: RealmList<Double> = RealmList()
}

fun MapPointRealm.fromRealm() = MapPoint(coordinates[1]
        ?: 0.0, coordinates[0] ?: 0.0)