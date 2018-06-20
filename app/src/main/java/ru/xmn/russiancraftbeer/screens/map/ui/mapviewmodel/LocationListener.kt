package ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import ru.xmn.common.extensions.hasLocationPermission

class LocationListener(val context: Context, private val locationListener: (Location) -> Unit) {
    private val locationRequest: LocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(3000).setFastestInterval(1500)

    private val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(context)
            .addApi(LocationServices.API)
            .build().apply {
                connect()
            }

    fun startListening() {
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

    fun stopListening() {
        if (googleApiClient.isConnected)
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener)
    }

    @SuppressLint("MissingPermission")
    private fun subscribeOnLocationChange() {
        if (!context.hasLocationPermission()) return
        LocationServices.FusedLocationApi
                .requestLocationUpdates(
                        googleApiClient,
                        locationRequest,
                        locationListener
                )
        LocationServices.FusedLocationApi
                .flushLocations(googleApiClient)
    }
}