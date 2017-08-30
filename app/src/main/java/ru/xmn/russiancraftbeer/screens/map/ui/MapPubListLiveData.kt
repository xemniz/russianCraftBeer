package ru.xmn.russiancraftbeer.screens.map.ui

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.content.Context
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.xmn.common.extensions.distanceTo
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import java.util.*

class MapPubListLiveData(val context: Context, val mapListUseCase: MapListUseCase) : LiveData<MapState>() {
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationRequest: LocationRequest
    private val listener: LocationListener
    private var hasPermission: Boolean = false
    private var subscribe: Disposable? = null

    private var currentLocation: LatLng = LatLng(55.751244, 37.618423)// moscow

    init {
        buildGoogleApiClient()
        subscribe()
        listener = LocationListener {
            val newLocation = LatLng(it.latitude, it.longitude)
            if (newLocation.distanceTo(currentLocation) > 1000) {
                currentLocation = LatLng(it.latitude, it.longitude)
                subscribe()
            }
        }
    }

    private fun buildGoogleApiClient() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000).setFastestInterval(1500)

        googleApiClient = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build()

        googleApiClient.connect()
    }

    fun subscribe() {
        subscribe?.apply {
            if (!isDisposed)
                dispose()
        }
        subscribe = mapListUseCase.getPubsForMap(currentLocation)
                .map<MapState> { MapState.Success(it, 0, UUID.randomUUID().toString()) }
                .startWith(MapState.Loading())
                .onErrorReturn { MapState.Error(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    value = it
                })
    }

    fun connectAndSubscribeOnLocationChange() {
        hasPermission = true
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
                        listener
                )
        LocationServices
                .FusedLocationApi
                .flushLocations(googleApiClient)
    }

    override fun onActive() {
        if (hasPermission)
            connectAndSubscribeOnLocationChange()
    }

    override fun onInactive() {
        if (hasPermission && googleApiClient.isConnected)
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, listener)
    }

    fun repeatLastLocation() {
        subscribe()
    }
}