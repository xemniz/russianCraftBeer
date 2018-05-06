package ru.xmn.russiancraftbeer.screens.map.ui.map

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import io.reactivex.subjects.BehaviorSubject
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.MapState
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.Focus


class MapViewManager(val activity: AppCompatActivity) {
    private lateinit var map: GoogleMap
    private lateinit var delegate: Delegate
    private lateinit var clusterManager: ClusterManager<PubClusterItem>
    private lateinit var pubClusterRenderer: PubClusterRenderer
    private val CLUSTERING_ZOOM = 12

    private val pubClusterItems: MutableList<PubClusterItem> = ArrayList()
    private var currentMarkerHighLight: Marker? = null
    private var currentMarkerItem: Marker? = null
    private var currentItem: PubClusterItem? = null

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
        map.getUiSettings().setZoomControlsEnabled(true)
        delegate.requestPermission()
        setListeners()

        itemsSubjects.subscribe {
            when {
                it is MapState.Loading -> {
                    //do nothing
                }
                it is MapState.Success -> {
                    if (it.listUniqueId != listUniqueId) {
                        showPubsOnMap(it.pubs)
                        listUniqueId = it.listUniqueId
                    }

                    val itemNumberToSelect = it.itemNumberToSelect

                    selectMarker(pubClusterItems[itemNumberToSelect], it.focus)
                }
                it is MapState.Error -> {
                    showPubsOnMap(emptyList())
                    dropPreviousMarkerHighlight()
                }
            }
        }
    }

    private fun setListeners() {
        map.setOnMapClickListener {
            dropPreviousMarkerHighlight()
            delegate.mapClick()
        }
        map.setOnMyLocationButtonClickListener {
            delegate.myPositionClick()

            return@setOnMyLocationButtonClickListener true
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
            if (zoom < CLUSTERING_ZOOM && isItemHighlighted())
                dropPreviousMarkerHighlight()
        }
    }

    private fun mapZoomIn(position: LatLng, zoom: Float) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                position, Math.floor((map.cameraPosition.zoom + zoom).toDouble()).toFloat()), 300,
                null)
    }

    @SuppressLint("MissingPermission")
    fun onPermissionGranted() {
        map.isMyLocationEnabled = true
    }

    //region api
    fun updateMap(mapState: MapState) {
        itemsSubjects.onNext(mapState)
    }

    fun isCurrentItemVisibleInMap(): Boolean {
        return isLocationInBounds() && isItemHighlighted()
    }
    //endregion

    private fun isItemHighlighted() = currentMarkerItem != null

    //текущая локация по умолчанию
    private fun isLocationInBounds(location: LatLng? = currentItem?.position): Boolean {
        val bounds = map.getProjection().getVisibleRegion().latLngBounds
        return bounds.contains(location)
    }

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

    private fun selectMarker(pubClusterItem: PubClusterItem, focus: Focus) {
        dropPreviousMarkerHighlight()

        currentItem = pubClusterItem
        highlightMarker(pubClusterItem)

        val zoom = if (map.cameraPosition.zoom < 15) 15f else map.cameraPosition.zoom
        val lastKnownLocation = map.myLocation?.let { LatLng(it.latitude, it.longitude) }
        val position = when (focus) {
            Focus.ON_MY_LOCATION -> {
                lastKnownLocation ?: pubClusterItem.position
            }
            Focus.ON_ITEM -> {
                pubClusterItem.position
            }
        }

        //отключить скрытие итема на движение карты
        map.setOnCameraMoveStartedListener(null)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                position,
                zoom
        ), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                currentItem?.let {
                    if (!isItemHighlighted()) {
                        //подсветить маркер повторно, если мы пришли из зума (при отдалении подсветка маркеров исчезает)
                        dropPreviousMarkerHighlight()
                        highlightMarker(it)
                    }
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
            currentMarkerHighLight = null
            currentMarkerItem = null
        } catch (e: Exception) {
        }
    }

    private fun markerClick(pubClusterItem: PubClusterItem): Boolean {
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
            selectMarker(it, Focus.ON_ITEM)
        }
    }
}

