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
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import rx.subjects.BehaviorSubject


class MapViewManager(val activity: AppCompatActivity) {
    private lateinit var map: GoogleMap
    private lateinit var delegate: Delegate
    private lateinit var clusterManager: ClusterManager<PubClusterItem>
    private lateinit var pubClusterRenderer: PubClusterRenderer

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
        map.setOnMapClickListener {
            delegate.mapClick()
        }
        map.getUiSettings().setZoomControlsEnabled(true)
        delegate.requestPermission()
        map.setOnMyLocationButtonClickListener {
            delegate.myPositionClick()
            return@setOnMyLocationButtonClickListener false
        }
        itemsSubjects.subscribe {
            showPubsOnMap(it)
            if (!alreadyGotFirstState) alreadyGotFirstState = true
        }
        clusterManager = ClusterManager(activity, map)
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        map.setOnInfoWindowClickListener(clusterManager)
        pubClusterRenderer = PubClusterRenderer(activity, map, clusterManager)
    }

    @SuppressLint("MissingPermission")
    fun onPermissionGranted() {
        map.isMyLocationEnabled = true
    }

    private val pubClusterItems: MutableList<PubClusterItem> = ArrayList()
    private var currentMarker: Marker? = null

    //region api
    fun pushMarkerPosition(position: Int) {
        if (!alreadyGotFirstState) return

        selectMarker(pubClusterItems[position])
    }

    private var alreadyGotFirstState: Boolean = false

    fun pushItems(pubs: List<PubMapDto>, currentItemPosition: Int) {
        itemsSubjects.onNext(MapItemsState(pubs, currentItemPosition))
    }

    fun isMarkerInBounds(position: Int): Boolean {
        val bounds = map.getProjection().getVisibleRegion().latLngBounds
        return bounds.contains(pubClusterItems[position].position)
    }
    //endregion

    private fun showPubsOnMap(mapItemsState: MapItemsState) {
        val (items, position) = mapItemsState

        clearMap()
        pubClusterItems += items.map { PubClusterItem(it) }
        clusterManager.addItems(pubClusterItems)
        clusterManager.renderer = pubClusterRenderer
        clusterManager.setOnClusterItemClickListener { item -> markerClick(item) }
        selectMarker(pubClusterItems[position])
    }

    private fun clearMap() {
        map.clear()
        currentMarker = null
        pubClusterItems.clear()
    }

    private fun selectMarker(pubClusterItem: PubClusterItem) {
        pubClusterRenderer.selectedClusterItem = pubClusterItem

        dropPreviousMarkerHighlight()
        pubClusterRenderer.getMarker(pubClusterItem)?.let { highlightMarker(it) }

        map.setOnCameraMoveStartedListener(null)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                pubClusterItem.position.run { LatLng(this.latitude - .0025, this.longitude) },
                15f
        ), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                pubClusterRenderer.getMarker(pubClusterItem)?.let { highlightMarker(it) }

                map.setOnCameraMoveStartedListener { delegate.cameraMove() }
            }

            override fun onCancel() {
            }
        }
        )
    }

    private fun highlightMarker(marker: Marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        marker.zIndex = 1f
        currentMarker = marker
    }

    private fun dropPreviousMarkerHighlight() {
        try {
            currentMarker?.apply {
                setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                zIndex = 0f
            }
        } catch (e: Exception) {
            currentMarker = null
        }
    }

    private fun markerClick(pubClusterItem: PubClusterItem): Boolean {
        selectMarker(pubClusterItem)
        delegate.markerClick(pubClusterItem.pubMapDto.uniqueTag)
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

class PubClusterRenderer(val activity: AppCompatActivity,
                         val map: GoogleMap,
                         clusterManager: ClusterManager<PubClusterItem>)
    : DefaultClusterRenderer<PubClusterItem>(activity, map, clusterManager) {
    var selectedClusterItem: PubClusterItem? = null

    override fun getColor(clusterSize: Int): Int {
        return activity.resources.getColor(R.color.colorAccent)
    }

    override fun onBeforeClusterItemRendered(item: PubClusterItem, markerOptions: MarkerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pub_marker))
        super.onBeforeClusterItemRendered(item, markerOptions)

    }

    override fun onClusterItemRendered(clusterItem: PubClusterItem?, marker: Marker?) {
        super.onClusterItemRendered(clusterItem, marker)
        if (clusterItem == selectedClusterItem){
            marker?.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            marker?.zIndex = 1f
        }
    }


}

class PubClusterItem(val pubMapDto: PubMapDto) : ClusterItem {

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return ""
    }

    override fun getPosition(): LatLng {
        return pubMapDto.map!![0].toLatLng()
    }

}
