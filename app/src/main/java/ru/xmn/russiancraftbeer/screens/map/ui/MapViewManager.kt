package ru.xmn.russiancraftbeer.screens.map.ui

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import rx.subjects.BehaviorSubject


class MapViewManager(val activity: AppCompatActivity) {
    private lateinit var map: GoogleMap
    private lateinit var delegate: Delegate

    private val itemsSubjects = BehaviorSubject.create<MapItemsState>()

    data class MapItemsState(val items: List<PubMapDto>, val position: Int)

    fun init(d: Delegate) {
        this.delegate = d
        val mapFragment = activity.supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this::mapReady)
    }

    private fun mapReady(it: GoogleMap) {
        map = it
        map.setOnMapClickListener { delegate.mapClick() }
        map.getUiSettings().setZoomControlsEnabled(true)
        delegate.requestPermission()
        map.setOnMyLocationButtonClickListener { delegate.myPositionClick(); return@setOnMyLocationButtonClickListener false }
        itemsSubjects.subscribe {
            showPubsOnMap(it)
            if (!alreadyGotFirstState) alreadyGotFirstState = true
        }
    }

    @SuppressLint("MissingPermission")
    fun onPermissionGranted() {
        map.isMyLocationEnabled = true
    }

    private val markers: MutableList<Marker> = ArrayList()
    private var currentMarker: Marker? = null

    //region api
    fun pushMarkerPosition(position: Int) {
        if (!alreadyGotFirstState) return

        selectMarker(markers[position])
    }

    private var alreadyGotFirstState: Boolean = false

    fun pushItems(pubs: List<PubMapDto>, currentItemPosition: Int) {
        itemsSubjects.onNext(MapItemsState(pubs, currentItemPosition))
    }

    fun isMarkerInBounds(position: Int): Boolean {
        val marker: Marker = markers[position]
        val bounds = map.getProjection().getVisibleRegion().latLngBounds
        return bounds.contains(marker.position)
    }
    //endregion

    private fun showPubsOnMap(mapItemsState: MapItemsState) {
        val (items, position) = mapItemsState

        clearMap()
        items.forEach({
            val pub = LatLng(it.map!![0].coordinates[1], it.map[0].coordinates[0])
            val marker = map.addMarker(MarkerOptions().position(pub).title(it.title))
            marker.tag = it.uniqueTag
            markers += marker
            map.setOnMarkerClickListener(this::markerClick)
        })

        selectMarker(markers[position])
    }

    private fun clearMap() {
        map.clear()
        currentMarker = null
        markers.clear()
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

    interface Delegate {
        fun mapClick()
        fun requestPermission()
        fun cameraMove()
        fun markerClick(tag: String)
        fun myPositionClick()
    }
}