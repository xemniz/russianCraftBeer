package ru.xmn.russiancraftbeer.screens.map.bl.data

import com.google.android.gms.maps.model.LatLng

data class MapPoint(val lat: Double, val long: Double) {
    companion object {
        fun from(l: LatLng) = MapPoint(l.latitude, l.longitude)
        fun moscow() = MapPoint(55.751244, 37.618423)
    }

    fun latLng(): LatLng {
        return LatLng(lat, long)
    }
}