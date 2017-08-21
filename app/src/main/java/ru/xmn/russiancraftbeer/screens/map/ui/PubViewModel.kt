package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.bl.PubUseCase
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import ru.xmn.russiancraftbeer.services.beer.PubDto
import javax.inject.Inject

class PubViewModel : ViewModel() {
    @Inject
    lateinit var pubUseCase: PubUseCase
    val mapState: MutableLiveData<PubState> = MutableLiveData()

    init {
        App.component.provideMapComponentBuilder.mapModule(MapModule()).build().inject(this)
    }

    fun clickPub(id: String, title: String, type: String) {
        pubUseCase.getPub(id)
                .map<PubState> { PubState.Success(it, title, type) }
                .startWith(PubState.Loading())
                .onErrorReturn { PubState.Error(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mapState.value = it })
    }
}

sealed class PubState() {
    class Success(val pub: PubDto, val title: String, val type: String) : PubState()
    class Error(private val e: Throwable) : PubState() {
        val errorMessage: String

        init {
            errorMessage = "Something went wrong"
        }
    }

    class Loading : PubState()
}