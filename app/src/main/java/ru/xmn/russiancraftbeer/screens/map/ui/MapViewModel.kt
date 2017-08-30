package ru.xmn.russiancraftbeer.screens.map.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.*
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
    val locationLiveData: LocationLiveData
    val mapState: LiveData<MapState>

    var currentItemTag: Int by Delegates.observable(0){
        property, oldValue, newValue ->
    }

    init {
        App.component.provideMapComponentBuilder.mapModule(MapModule()).build().inject(this)
        locationLiveData = LocationLiveData(context)
        mapState = Transformations.switchMap(locationLiveData,
                { location: LatLng -> MapPubListLiveData(location, mapListUseCase) })
    }

    fun refresh() {
        locationLiveData.repeatLastLocation()
    }

    class Factory(val nid: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PubViewModel(nid) as T
        }

    }
}

sealed class MapState {
    class Success(val pubs: List<PubMapDto>, val currentItemPosition: Int) : MapState()

    class Error(val e: Throwable) : MapState() {
        val errorMessage: String

        init {
            e.printStackTrace()
            errorMessage = "Something went wrong"
        }
    }

    class Loading : MapState()

}