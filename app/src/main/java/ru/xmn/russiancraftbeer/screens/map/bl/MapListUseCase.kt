package ru.xmn.russiancraftbeer.screens.map.bl

import io.reactivex.Flowable
import ru.xmn.russiancraftbeer.services.beer.MapPoint
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import ru.xmn.russiancraftbeer.services.beer.PubRepository


class MapListUseCase(private val repository: PubRepository) {
    fun getPabsForMap(mapPoint: MapPoint): Flowable<List<PubMapDto>> {
        return repository.getPubListMap()
                .map { allPubsToUnique(it) }
                .map {
                    it.sortedWith(Comparator({
                        a, b ->
                        comparePoints(mapPoint, a.map!![0], b.map!![0])
                    }))
                }
    }

    private fun comparePoints(target: MapPoint, a: MapPoint, b: MapPoint): Int {
        val (lon1, lat1) = a.coordinates
        val (lon2, lat2) = b.coordinates
        val (lon0, lat0) = target.coordinates

        val distanceToPlace1 = distance(lat0, lon0, lat1, lon1)
        val distanceToPlace2 = distance(lat0, lon0, lat2, lon2)
        return ((distanceToPlace1 - distanceToPlace2).toInt())
    }

    private fun distance(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Double {
        val radius = 6378137.0   // approximate Earth radius, *in meters*
        val deltaLat = toLat - fromLat
        val deltaLon = toLon - fromLon
        val angle = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2.0) + Math.cos(fromLat) * Math.cos(toLat) *
                        Math.pow(Math.sin(deltaLon / 2), 2.0)))
        return radius * angle
    }

    //бывают пабы с несколькими адресами. для отображения вычленяем адреса, координаты в отдельные объекты
    private fun allPubsToUnique(list: List<PubMapDto>): List<PubMapDto> {
        val fold = list.fold(ArrayList<PubMapDto>(), { r, it ->
            if (it.address!!.size > 1) {
                val mapIndexed = it.address.mapIndexed { index, s ->
                    it.copy(map = listOf(it.map!![index]), address = listOf(it.address[index]))
                }
                r.addAll(mapIndexed)
            } else
                r.add(it)
            r
        })
        return fold
    }

}