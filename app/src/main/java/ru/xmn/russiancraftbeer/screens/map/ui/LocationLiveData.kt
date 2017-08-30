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
import ru.xmn.common.extensions.distanceTo


/**
 * Created by USER on 30.08.2017.
 */
class LocationLiveData(val context: Context) : LiveData<LatLng>() {
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var listener: LocationListener
    private var hasPermission: Boolean = false

    init {
        buildGoogleApiClient()
        value = LatLng(55.751244, 37.618423) // moscow
        listener = LocationListener {
            val newLocation = LatLng(it.latitude, it.longitude)
            if (newLocation.distanceTo(value!!) > 1000) {
                value = newLocation
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
        value = value
    }
}