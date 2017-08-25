package ru.xmn.russiancraftbeer.screens.map.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import com.google.android.gms.maps.model.LatLngBounds



class MapViewManager(val activity: AppCompatActivity) {
    private lateinit var map: GoogleMap
    private lateinit var googleApiClient: GoogleApiClient
    private lateinit var locationRequest: LocationRequest

    private lateinit var delegate: Delegate

    private var lastLocation: LatLng? = null


    private val EMPTY_LOCATION = LatLng(55.751244, 37.618423) // moscow

    fun init(d: Delegate) {
        this.delegate = d
        val mapFragment = activity.supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync({
            map = it
            map.setOnMapClickListener { delegate.mapClick() }
            map.getUiSettings().setZoomControlsEnabled(true);

            val checkPermision = ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (checkPermision)
                showRefreshedLocation()
            else {
                delegate.requestPermission()
                delegate.locationChange(EMPTY_LOCATION)
            }

            map.setOnMyLocationButtonClickListener { delegate.myPositionClick(); return@setOnMyLocationButtonClickListener false }
        })
    }

    fun buildGoogleApiClient(function: () -> Unit) {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(3000).setFastestInterval(1500)

        googleApiClient = GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(p0: Bundle?) {
                        function()
                    }

                    override fun onConnectionSuspended(p0: Int) {
                        println("suspended")
                    }
                })
                .addOnConnectionFailedListener({ t ->
                    println("failed")
                    t.errorMessage
                })
                .addApi(LocationServices.API)
                .build()

        googleApiClient.connect()
    }

    @SuppressLint("MissingPermission")
    fun showRefreshedLocation() {
        map.isMyLocationEnabled = true

        buildGoogleApiClient{

            LocationServices
                    .FusedLocationApi
                    .requestLocationUpdates(
                            googleApiClient,
                            locationRequest,
                            {
                                if (lastLocation == null) {
                                    lastLocation = LatLng(it.latitude, it.longitude)
                                    delegate.locationChange(LatLng(it.latitude, it.longitude))
                                }
                            }
                    )
            LocationServices
                    .FusedLocationApi
                    .flushLocations(googleApiClient)
        }


    }

    private val markers: MutableList<Marker> = ArrayList()
    private var currentMarker: Marker? = null

    fun showPubsOnMap(pubs: List<PubMapDto>) {
        clearMap()
        pubs.forEach({
            val pub = LatLng(it.map!![0].coordinates[1], it.map[0].coordinates[0])
            val marker = map.addMarker(MarkerOptions().position(pub).title(it.title))
            marker.tag = it.uniqueTag
            markers += marker
            map.setOnMarkerClickListener(this::markerClick)
        })
        selectMarker(0)
    }

    private fun clearMap() {
        map.clear()
        currentMarker = null
        markers.clear()
    }

    fun selectMarker(position: Int) {
        selectMarker(markers[position])
    }

    private fun selectMarker(marker: Marker) {
        highlightMarker(marker)

        map.setOnCameraMoveStartedListener(null)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                marker.position.run { LatLng(this.latitude - .0025, this.longitude) },
                15f
        ), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                map.setOnCameraMoveStartedListener { delegate.cameraMove() }
            }

            override fun onCancel() {
            }
        }
        )
    }

    private fun highlightMarker(marker: Marker) {
        currentMarker?.apply {
            setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            zIndex = 0f
        }
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        marker.zIndex = 1f
        currentMarker = marker
    }

    private fun markerClick(m: Marker): Boolean {
        selectMarker(m)
        delegate.markerClick(m.tag as String)
        return true
    }

    fun selectMarkerIfNotInBounds(position: Int): Boolean {
        val marker: Marker = markers[position]
        val bounds = map.getProjection().getVisibleRegion().latLngBounds
        if (bounds.contains(marker.position)) {
            return false
        } else {
            selectMarker(marker)
            return true
        }
    }

    interface Delegate {
        fun mapClick()
        fun locationChange(l: LatLng)
        fun requestPermission()
        fun cameraMove()
        fun markerClick(tag: String)
        fun myPositionClick()

    }
}