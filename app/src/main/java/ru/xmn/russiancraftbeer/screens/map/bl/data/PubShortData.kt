package ru.xmn.russiancraftbeer.screens.map.bl.data

data class PubShortData(
        val mapPoint: MapPoint,
        val address: String,
        val nid: String,
        val type: String,
        val title: String,
        val image: String) {
    val uniqueTag: String
        get() = address
}