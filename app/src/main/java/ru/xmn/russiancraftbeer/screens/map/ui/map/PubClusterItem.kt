package ru.xmn.russiancraftbeer.screens.map.ui.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import ru.xmn.russiancraftbeer.services.beer.PubMapDto

class PubClusterItem(val pubMapDto: PubMapDto, var selected: Boolean = false) : ClusterItem {

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return pubMapDto.title ?: ""
    }

    override fun getPosition(): LatLng {
        return pubMapDto.map!![0].toLatLng()
    }

}