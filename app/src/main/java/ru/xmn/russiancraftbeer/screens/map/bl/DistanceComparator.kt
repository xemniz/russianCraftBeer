package ru.xmn.russiancraftbeer.screens.map.bl

import com.google.android.gms.maps.model.LatLng
import ru.xmn.common.extensions.distanceTo
import ru.xmn.russiancraftbeer.screens.map.bl.data.MapPoint
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubShortData

/**
 * Сортирует по удаленности от заданной точки
 */
class DistanceComparator(private val location: MapPoint) : Comparator<PubShortData> {
    override fun compare(o1: PubShortData, o2: PubShortData): Int {
        return comparePoints(location, o1.mapPoint, o2.mapPoint)
    }

    private fun comparePoints(target: MapPoint, a: MapPoint, b: MapPoint): Int {
        val (lat1, lon1) = a
        val (lat2, lon2) = b
        val (lat0, lon0) = target

        val distanceToPlace1 = distance(lat0, lon0, lat1, lon1)
        val distanceToPlace2 = distance(lat0, lon0, lat2, lon2)
        return ((distanceToPlace1 - distanceToPlace2).toInt())
    }

    private fun distance(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Float {
        return LatLng(fromLat, fromLon).distanceTo(LatLng(toLat, toLon))
    }
}