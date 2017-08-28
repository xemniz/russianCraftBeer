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
import com.google.maps.android.clustering.Cluster
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
        clusterManager = ClusterManager(activity, map)
        pubClusterRenderer = PubClusterRenderer(activity, map, clusterManager)
        clusterManager.renderer = pubClusterRenderer
        clusterManager.setOnClusterItemClickListener { markerClick(it) }

        map.setOnMapClickListener {
            delegate.mapClick()
        }
        map.getUiSettings().setZoomControlsEnabled(true)
        delegate.requestPermission()
        map.setOnMyLocationButtonClickListener {
            delegate.myPositionClick()
            return@setOnMyLocationButtonClickListener false
        }
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener {
            return@setOnMarkerClickListener when {
                (it==currentMarkerHighLight || it == currentMarkerItem) && currentItem != null ->
                    markerClick(currentItem!!)
                else ->
                    clusterManager.onMarkerClick(it)
            }
        }
        map.setOnInfoWindowClickListener(clusterManager)
        clusterManager.setOnClusterClickListener({ cluster ->
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    cluster.getPosition(), Math.floor((map.cameraPosition.zoom + 1).toDouble()).toFloat()), 300,
                    null)
            true
        })
        map.setOnCameraMoveListener { pubClusterRenderer.zoom = map.cameraPosition.zoom }

        itemsSubjects.subscribe {
            showPubsOnMap(it)
            if (!alreadyGotFirstState) alreadyGotFirstState = true
        }
    }

    @SuppressLint("MissingPermission")
    fun onPermissionGranted() {
        map.isMyLocationEnabled = true
    }

    private val pubClusterItems: MutableList<PubClusterItem> = ArrayList()
    private var currentMarkerHighLight: Marker? = null
    private var currentMarkerItem: Marker? = null
    var currentItem: PubClusterItem? = null

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
        selectMarker(pubClusterItems[position])
    }

    private fun clearMap() {
        map.clear()
        currentMarkerHighLight = null
        pubClusterItems.clear()
    }

    private fun selectMarker(pubClusterItem: PubClusterItem) {
        dropPreviousMarkerHighlight()

        currentItem = pubClusterItem
        highlightMarker(pubClusterItem)

        map.setOnCameraMoveStartedListener(null)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                pubClusterItem.position.run { LatLng(this.latitude - .0025, this.longitude) },
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

    private fun highlightMarker(currentPubItem: PubClusterItem) {
        currentMarkerItem = map.addMarker(MarkerOptions().apply {
            position(currentPubItem.position)
            when {
                currentPubItem.pubMapDto.type == "Магазины" -> {
                    zIndex(3f)
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.map_store_icon))
                    anchor(.5f, .5f)
                }
                else -> {
                    zIndex(3f)
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pub_icon))
                    anchor(.45f, .5f)
                }
            }
        })
        currentMarkerHighLight = map.addMarker(MarkerOptions().apply {
            position(currentPubItem.position)
            zIndex(2f)
            icon(BitmapDescriptorFactory.fromResource(R.drawable.map_highlight))
            anchor(.5f, .5f)
            alpha(.6f)
        })

//        clusterManager.removeItem(currentPubItem)
//        clusterManager.cluster()
    }

    private fun dropPreviousMarkerHighlight() {
//        currentItem?.let {clusterManager.addItem(currentItem)}
//        clusterManager.cluster()
        try {
            currentMarkerHighLight?.remove()
            currentMarkerItem?.remove()
        } catch(e: Exception) {
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
    var zoom: Float = 15f

    override fun getColor(clusterSize: Int): Int {
        return activity.resources.getColor(R.color.colorAccent)
    }

    override fun onBeforeClusterItemRendered(item: PubClusterItem, markerOptions: MarkerOptions) {
        when {
            item.pubMapDto.type == "Магазины" -> {
                markerOptions.zIndex(1f)
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_store_icon))
                markerOptions.anchor(.5f, .5f)
            }
            else -> {
                markerOptions.zIndex(1f)
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pub_icon))
                markerOptions.anchor(.45f, .5f)
            }
        }
        super.onBeforeClusterItemRendered(item, markerOptions)
    }

    override fun onClusterItemRendered(clusterItem: PubClusterItem?, marker: Marker?) {
        super.onClusterItemRendered(clusterItem, marker)
    }

    override fun shouldRenderAsCluster(cluster: Cluster<PubClusterItem>): Boolean {
        return when {
            zoom < 12 -> return true
            else -> super.shouldRenderAsCluster(cluster)
        }
    }


}

class PubClusterItem(val pubMapDto: PubMapDto, val selected: Boolean = false) : ClusterItem {

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
