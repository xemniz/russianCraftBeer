package ru.xmn.russiancraftbeer.screens.map.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.xmn.common.extensions.distanceTo
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import ru.xmn.russiancraftbeer.services.beer.MapPoint
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import javax.inject.Inject

class MapViewModel : ViewModel() {
    @Inject lateinit var mapListUseCase: MapListUseCase
    @SuppressLint("StaticFieldLeak")
    @Inject lateinit var context: Context
    val mapState: MutableLiveData<MapState> = MutableLiveData()
    var currentItemPosition: Int = 0
    private var lastLocation: LatLng = LatLng(55.751244, 37.618423) // moscow by default

    init {
        App.component.provideMapComponentBuilder.mapModule(MapModule()).build().inject(this)
        buildGoogleApiClient()
        refresh()
    }

    private var subscribe: Disposable? = null

    fun request(mapPoint: MapPoint) {
        subscribe?.dispose()

        subscribe = mapListUseCase.getPubsForMap(mapPoint)
                .map<MapState> { MapState.Success(it, currentItemPosition) }
                .startWith(MapState.Loading())
                .onErrorReturn { MapState.Error(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mapState.value = it })
    }

    fun refresh() {
        request(MapPoint.from(lastLocation))
    }

    //request location
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationRequest: LocationRequest

    fun onPermissionGranted() {
        if (googleApiClient.isConnected) {
            subscribeOnLocationChange()
        } else {
            val connectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(p0: Bundle?) {
                    subscribeOnLocationChange()
                    googleApiClient.unregisterConnectionCallbacks(this)
                }

                override fun onConnectionSuspended(p0: Int) {
                }
            }
            googleApiClient.registerConnectionCallbacks(connectionCallbacks)
        }
    }

    @SuppressLint("MissingPermission")
    private fun subscribeOnLocationChange() {
        LocationServices
                .FusedLocationApi
                .requestLocationUpdates(
                        googleApiClient,
                        locationRequest,
                        {
                            val newLocation = LatLng(it.latitude, it.longitude)
                            if (newLocation.distanceTo(lastLocation) > 1000) {
                                lastLocation = newLocation
                                request(MapPoint.from(lastLocation))
                            }
                        }
                )
        LocationServices
                .FusedLocationApi
                .flushLocations(googleApiClient)
    }

    fun buildGoogleApiClient() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000).setFastestInterval(1500)

        googleApiClient = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build()

        googleApiClient.connect()
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