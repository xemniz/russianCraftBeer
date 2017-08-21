package ru.xmn.russiancraftbeer.screens.map.bl

import io.reactivex.Flowable
import ru.xmn.russiancraftbeer.services.beer.PubDto
import ru.xmn.russiancraftbeer.services.beer.PubRepository

class PubUseCase(private val repository: PubRepository) {
    fun getPub(id: String): Flowable<PubDto> {
        return repository.getPub(id)
    }
}