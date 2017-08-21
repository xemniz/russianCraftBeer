package ru.xmn.russiancraftbeer.screens.map.bl

import io.reactivex.Flowable
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import ru.xmn.russiancraftbeer.services.beer.PubRepository

class MapListUseCase(private val repository: PubRepository) {
    fun getPabsForMap(): Flowable<List<PubMapDto>> {
        return repository.getPubListMap()
                .map { allPubsToUnique(it) }
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