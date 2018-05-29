package ru.xmn.russiancraftbeer.services.beer.data

import ru.xmn.common.extensions.stripHtml
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubFullData

class PubFullDataMapper {
    fun map(value: PubFullDataRealm) = value.fromRealm()
}

fun PubFullDataRealm.fromRealm(): PubFullData {
    if (nid.isNullOrBlank()) throw IllegalArgumentException("can't create PubFullData with empty nid")

    return PubFullData(
            nid = nid!!,
            description = body?.stripHtml() ?: "",
            addresses = address ?: emptyList(),
            mapPoints = map?.map { it.fromRealm() } ?: emptyList(),
            phones = phones ?: emptyList(),
            webSites = site ?: emptyList()
    )
}