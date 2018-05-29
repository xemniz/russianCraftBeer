package ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.content.Context
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubShortData
import javax.inject.Inject
import kotlin.properties.Delegates

class MapViewModel : ViewModel() {
    @Inject lateinit var mapListUseCase: MapListUseCase
    @SuppressLint("StaticFieldLeak")
    @Inject lateinit var context: Context

    private var mapStateFromNetwork: MapPubListLiveData
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

    fun reinit(){
        mapStateFromNetwork = MapPubListLiveData(context, mapListUseCase)
    }

    fun onPermissionGranted() {
        mapStateFromNetwork.connectAndSubscribeOnLocationChange()
    }

    fun refresh() {
        mapStateFromNetwork.repeatLastLocation()
    }

    fun selectMyLocation() {
        currentItemLiveData.pushCurrentItemPosition(0, Focus.ON_MY_LOCATION)
    }
}

sealed class MapState {
    class Success(val pubs: List<PubShortData>, val itemNumberToSelect: Int, val listUniqueId: String, val focus: Focus = Focus.ON_ITEM) : MapState()

    class Error(val e: Throwable) : MapState() {
        val errorMessage: String

        init {
            e.printStackTrace()
            errorMessage = "Something went wrong"
        }
    }

    class Loading : MapState()

}