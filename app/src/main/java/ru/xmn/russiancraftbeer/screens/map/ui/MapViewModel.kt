package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import ru.xmn.russiancraftbeer.services.beer.MapPoint
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import javax.inject.Inject

class MapViewModel : ViewModel() {
    @Inject
    lateinit var mapListUseCase: MapListUseCase
    val mapState: MutableLiveData<MapState> = MutableLiveData()

    init {
        App.component.provideMapComponentBuilder.mapModule(MapModule()).build().inject(this)
    }

    private var subscribe: Disposable? = null

    fun request(mapPoint: MapPoint) {
        subscribe?.dispose()

        subscribe = mapListUseCase.getPabsForMap(mapPoint)
                .map<MapState> { MapState.Success(it) }
                .startWith(MapState.Loading())
                .onErrorReturn { MapState.Error(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mapState.value = it })
    }
}

sealed class MapState {
    class Success(val pubs: List<PubMapDto>) : MapState()
    class Error(private val e: Throwable) : MapState() {
        val errorMessage: String

        init {
            e.printStackTrace()
            errorMessage = "Something went wrong"
        }
    }

    class Loading : MapState()
}