package ru.xmn.russiancraftbeer.screens.map.bl.data

import ru.xmn.russiancraftbeer.screens.map.bl.DistanceComparator

data class PubShortData(
        val mapPoint: MapPoint,
        val address: String,
        val nid: String,
        val type: String,
        val title: String,
        val image: String) {
    val tag: String
        get() = address+title
}

typealias Pubs = List<PubShortData>
fun Pubs.firstWithTag(tag: String?) = firstOrNull{it.tag == tag}
fun Pubs.sortedByClosest(mapPoint: MapPoint): Pubs =
        sortedWith(DistanceComparator(mapPoint))
val Pubs.firstPub get() = firstOrNull()
val Pubs.initialTag get() = firstPub?.tag
val Pubs.initialFocus get() = firstPub?.mapPoint
