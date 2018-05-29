package ru.xmn.russiancraftbeer.screens.map.bl.data

import com.google.android.gms.maps.model.LatLng

data class MapPoint(val lat: Double, val long: Double) {
    companion object {
        fun from(l: LatLng) = MapPoint(l.latitude, l.longitude)
    }

    fun latLng(): LatLng {
        return LatLng(lat, long)
    }
}