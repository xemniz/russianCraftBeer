package ru.xmn.russiancraftbeer.screens.map.ui.map

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubShortData

class PubClusterItem(val pubShortData: PubShortData, var selected: Boolean = false) : ClusterItem {

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return pubShortData.title
    }

    override fun getPosition(): LatLng {
        return pubShortData.mapPoint.latLng()
    }

}