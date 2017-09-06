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
    val CLUSTERING_ZOOM = 12

    private val itemsSubjects = BehaviorSubject.create<MapState>()

    fun init(d: Delegate) {
        this.delegate = d
        val mapFragment = activity.supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this::mapReady)
    }

    private var listUniqueId: String = ""

    private fun mapReady(it: GoogleMap) {
        map = it
        clusterManager = ClusterManager(activity, map)
        pubClusterRenderer = PubClusterRenderer(activity, map, clusterManager, CLUSTERING_ZOOM)
        clusterManager.renderer = pubClusterRenderer
        clusterManager.setOnClusterItemClickListener { markerClick(it) }

        map.setPadding(0, 0, 0, activity.resources.getDimension(R.dimen.view_pager_collapsed_height).toInt())
        map.setOnMapClickListener {
            dropPreviousMarkerHighlight()
            delegate.mapClick()
        }
        map.getUiSettings().setZoomControlsEnabled(true)
        delegate.requestPermission()
        map.setOnMyLocationButtonClickListener {
            mapZoomIn(map.cameraPosition.target, 15f)
            delegate.myPositionClick()
            return@setOnMyLocationButtonClickListener false
        }
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener {
            return@setOnMarkerClickListener when {
                (it == currentMarkerHighLight || it == currentMarkerItem) && currentItem != null ->
                    markerClick(currentItem!!)
                else ->
                    clusterManager.onMarkerClick(it)
            }
        }
        map.setOnInfoWindowClickListener(clusterManager)
        clusterManager.setOnClusterClickListener({ cluster ->
            dropPreviousMarkerHighlight()
            mapZoomIn(cluster.getPosition(), 2f)
            true
        })
        map.setOnCameraMoveListener {
            val zoom = map.cameraPosition.zoom
            pubClusterRenderer.current_zoom = zoom
            if (zoom < CLUSTERING_ZOOM && currentMarkerItem != null)
                dropPreviousMarkerHighlight()
        }

        itemsSubjects.subscribe {
            when {
                it is MapState.Loading -> {
                }
                it is MapState.Success -> {

                    if (it.listUniqueId != listUniqueId) {
                        showPubsOnMap(it.pubs)
                        listUniqueId = it.listUniqueId
                    }

                    if (!itemIsSelected(it.currentItemPosition))
                        selectMarker(pubClusterItems[it.currentItemPosition])

                    val location = it.pubs[it.currentItemPosition].map!![0].toLatLng()
                    if (isLocationInBounds(location))
                        selectMarker(pubClusterItems[it.currentItemPosition])
                }
                it is MapState.Error -> {
                    showPubsOnMap(emptyList())
                    dropPreviousMarkerHighlight()
                }
            }
        }
    }

    private fun itemIsSelected(it: Int) = pubClusterItems.indexOf(currentItem) == it && currentMarkerItem == null

    private fun mapZoomIn(position: LatLng, zoom: Float) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                position, Math.floor((map.cameraPosition.zoom + zoom).toDouble()).toFloat()), 300,
                null)
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
    fun updateMap(mapState: MapState) {
        itemsSubjects.onNext(mapState)
    }

    fun isLocationInBounds(location: LatLng? = currentItem?.position): Boolean {
        val bounds = map.getProjection().getVisibleRegion().latLngBounds
        return bounds.contains(location)
    }
    //endregion

    private fun showPubsOnMap(items: List<PubMapDto>) {
        clearMap()
        pubClusterItems += items.map { PubClusterItem(it) }
        clusterManager.addItems(pubClusterItems)
    }

    private fun clearMap() {
        map.clear()
        clusterManager.clearItems()
        currentMarkerHighLight = null
        currentItem = null
        currentMarkerItem = null
        currentMarkerHighLight = null
        pubClusterItems.clear()
    }

    private fun selectMarker(pubClusterItem: PubClusterItem) {
        dropPreviousMarkerHighlight()

        currentItem = pubClusterItem
        highlightMarker(pubClusterItem)

        map.setOnCameraMoveStartedListener(null)
        val zoom = if (map.cameraPosition.zoom < 15) 15f else map.cameraPosition.zoom
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                pubClusterItem.position,
                zoom
        ), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                currentItem?.let {
                    if (!itemIsSelected(pubClusterItems.indexOf(it)))
                        dropPreviousMarkerHighlight()
                    highlightMarker(it)
                }
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
    }

    private fun dropPreviousMarkerHighlight() {
        try {
            currentMarkerHighLight?.remove()
            currentMarkerItem?.remove()
        } catch (e: Exception) {
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

    fun animateToCurrentItem() {
        currentItem?.let {
            selectMarker(it)
        }
    }
}

class PubClusterRenderer(val activity: AppCompatActivity,
                         val map: GoogleMap,
                         clusterManager: ClusterManager<PubClusterItem>, val CLUSTERING_ZOOM: Int)
    : DefaultClusterRenderer<PubClusterItem>(activity, map, clusterManager) {
    var current_zoom: Float = 15f

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
            current_zoom < CLUSTERING_ZOOM -> return true
            else -> super.shouldRenderAsCluster(cluster)
        }
    }


}

class PubClusterItem(val pubMapDto: PubMapDto, var selected: Boolean = false) : ClusterItem {

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return pubMapDto.title ?: ""
    }

    override fun getPosition(): LatLng {
        return pubMapDto.map!![0].toLatLng()
    }

}
