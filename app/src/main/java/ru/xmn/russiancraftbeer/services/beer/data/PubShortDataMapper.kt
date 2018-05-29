package ru.xmn.russiancraftbeer.services.beer.data

import io.realm.RealmList
import ru.xmn.common.extensions.stripHtml
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubShortData

class PubShortDataMapper {
    fun map(value: List<PubShortDataRealm>) =
            value.filter(::hasMapPointsAndAddresses)
                    .allPubsToUnique()
                    .map(::fromRealm)

    private fun hasMapPointsAndAddresses(pubShortDataRealm: PubShortDataRealm) =
            pubShortDataRealm.map?.isNotEmpty() ?: false && pubShortDataRealm.address?.isNotEmpty() ?: false

    //бывают пабы с несколькими адресами. для отображения вычленяем адреса, координаты в отдельные объекты
    private fun List<PubShortDataRealm>.allPubsToUnique(): List<PubShortDataRealm> {
        return fold(ArrayList(), { arrayList, pubShortDataRealm ->
            if (pubShortDataRealm.map!!.size > 1) {
                val pubsFlattenByMapPoint = pubShortDataRealm.map!!.mapIndexed { i, _ ->
                    val mapPointForIndex = pubShortDataRealm.map!![i]
                    val addressForMapPoint = try {
                        pubShortDataRealm.address!![i]
                    } catch (t: Throwable) {
                        ""
                    }
                    pubShortDataRealm.copyWith(mapPointForIndex, addressForMapPoint)
                }
                arrayList.addAll(pubsFlattenByMapPoint)
            } else
                arrayList.add(pubShortDataRealm)
            arrayList
        })
    }

    private fun PubShortDataRealm.copyWith(mapPointForIndex: MapPointRealm?, addressForMapPoint: String?): PubShortDataRealm {
        return copy(map = RealmList<MapPointRealm>().apply { add(mapPointForIndex) },
                address = RealmList<String>().apply { add(addressForMapPoint) })
    }

    private fun fromRealm(pubShortDataRealm: PubShortDataRealm): PubShortData {
        if (pubShortDataRealm.nid.isNullOrBlank()) throw IllegalArgumentException("can't create PubShortData with empty nid")

        return PubShortData(
                nid = pubShortDataRealm.nid!!,
                address = pubShortDataRealm.address!![0]!!,
                mapPoint = pubShortDataRealm.map!![0]!!.fromRealm(),
                type = pubShortDataRealm.type ?: "",
                title = pubShortDataRealm.title?.stripHtml() ?: "",
                image = pubShortDataRealm.field_logo?.substringBefore("\" width")?.substringAfter("src=\"")
                        ?: ""
        )
    }
}