package ru.xmn.russiancraftbeer.screens.map.ui.map

import android.support.v7.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import ru.xmn.russiancraftbeer.R

class PubClusterRenderer(val activity: AppCompatActivity,
                         val map: GoogleMap,
                         clusterManager: ClusterManager<PubClusterItem>, val CLUSTERING_ZOOM: Int)
    : DefaultClusterRenderer<PubClusterItem>(activity, map, clusterManager) {
    var currentZoom: Float = 15f

    override fun getColor(clusterSize: Int): Int {
        return activity.resources.getColor(R.color.colorAccent)
    }

    override fun onBeforeClusterItemRendered(item: PubClusterItem, markerOptions: MarkerOptions) {
        when {
            item.pubShortData.type == "Магазины" -> {
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

    override fun shouldRenderAsCluster(cluster: Cluster<PubClusterItem>): Boolean {
        return when {
            currentZoom < CLUSTERING_ZOOM -> return true
            else -> super.shouldRenderAsCluster(cluster)
        }
    }


}