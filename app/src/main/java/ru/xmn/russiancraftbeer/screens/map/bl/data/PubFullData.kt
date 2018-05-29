package ru.xmn.russiancraftbeer.screens.map.bl.data

data class PubFullData(
        val nid: String,
        val description: String,
        val addresses: List<String>,
        val mapPoints: List<MapPoint>,
        val phones: List<String>,
        val webSites: List<String>
) {

    companion object {
        fun empty() = PubFullData("", "", emptyList(), emptyList(), emptyList(), emptyList())
    }
}