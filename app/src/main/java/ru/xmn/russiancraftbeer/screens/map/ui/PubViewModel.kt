package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.bl.PubUseCase
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import ru.xmn.russiancraftbeer.services.beer.PubDto
import javax.inject.Inject

class PubViewModel(val nid: String) : ViewModel() {
    @Inject
    lateinit var pubUseCase: PubUseCase
    val mapState: MutableLiveData<PubState> = MutableLiveData()
    private var subscribe: Disposable? = null

    init {
        App.component.provideMapComponentBuilder.mapModule(MapModule()).build().inject(this)

        refresh()
    }

    fun refresh() {
        clickPub(nid)
    }

    private fun clickPub(id: String) {
        subscribe?.dispose()
        subscribe = pubUseCase.getPub(id)
                .map<PubState> { PubState.Success(it) }
                .startWith(PubState.Loading())
                .onErrorReturn { PubState.Error(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mapState.value = it })
    }

    class Factory(val nid: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PubViewModel(nid) as T
        }
    }
}

sealed class PubState {
    class Success(val pub: PubDto) : PubState()
    class Error(private val e: Throwable) : PubState() {
        val errorMessage: String

        init {
            e.printStackTrace()
            errorMessage = "Something went wrong"
        }
    }

    class Loading : PubState()
}