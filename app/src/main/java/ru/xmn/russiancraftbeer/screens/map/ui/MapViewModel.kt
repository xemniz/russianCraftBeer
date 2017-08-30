package ru.xmn.russiancraftbeer.screens.map.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.google.android.gms.maps.model.LatLng
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import javax.inject.Inject
import kotlin.properties.Delegates

class MapViewModel : ViewModel() {
    @Inject lateinit var mapListUseCase: MapListUseCase
    @SuppressLint("StaticFieldLeak")
    @Inject lateinit var context: Context

    private val mapStateFromNetwork: MapPubListLiveData
    private val currentItemLiveData: CurrentPubItemLiveData = CurrentPubItemLiveData()
    val mapState: LiveData<MapState>

    var currentItemPosition: Int by Delegates.observable(0) {
        _, _, newValue ->
        currentItemLiveData.pushCurrentItemPosition(newValue)
    }

    init {
        App.component.provideMapComponentBuilder.mapModule(MapModule()).build().inject(this)
        mapStateFromNetwork = MapPubListLiveData(context, mapListUseCase)

        mapState = Transformations.switchMap(mapStateFromNetwork,
                { mapState: MapState -> currentItemLiveData.pushNewState(mapState) })
    }

    fun onPermissionGranted() {
        mapStateFromNetwork.connectAndSubscribeOnLocationChange()
    }

    fun refresh() {
        mapStateFromNetwork.repeatLastLocation()
    }

    class Factory(val nid: String) : ViewModelProvider.Factory {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PubViewModel(nid) as T
        }

    }
}

sealed class MapState {
    class Success(val pubs: List<PubMapDto>, val currentItemPosition: Int, val listUniqueId: String) : MapState()

    class Error(val e: Throwable) : MapState() {
        val errorMessage: String

        init {
            e.printStackTrace()
            errorMessage = "Something went wrong"
        }
    }

    class Loading : MapState()

}