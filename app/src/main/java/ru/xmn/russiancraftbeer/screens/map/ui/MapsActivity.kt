package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import org.jetbrains.anko.toast
import ru.xmn.common.extensions.dp
import ru.xmn.common.extensions.px
import ru.xmn.common.widgets.BottomSheetBehaviorGoogleMapsLike
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.services.beer.PubMapDto

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LifecycleRegistryOwner {

    private lateinit var map: GoogleMap
    private lateinit var mapViewModel: MapViewModel
    private lateinit var pubViewModel: PubViewModel

    private lateinit var behavior: BottomSheetBehavior<ViewPager>

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry {
        return registry
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setupToolbar()
        setupMap()
        setupBehaviors()
        viewPager.adapter = PubPagerAdapter(this,
                {s -> ViewModelProviders.of(this, PubViewModel.Factory(s.nid)).get(s.nid, PubViewModel::class.java)})
    }

    private fun setupToolbar() {
//        val toolbar = toolbar
//        setSupportActionBar(toolbar)
//        supportActionBar?.setTitle("Beer map")
    }

    private fun setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupBehaviors() {
        val bottomSheet = viewPager
        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED -> Log.d("bottomsheet-", "STATE_COLLAPSED")
                    BottomSheetBehaviorGoogleMapsLike.STATE_DRAGGING -> Log.d("bottomsheet-", "STATE_DRAGGING")
                    BottomSheetBehaviorGoogleMapsLike.STATE_EXPANDED -> Log.d("bottomsheet-", "STATE_EXPANDED")
                    BottomSheetBehaviorGoogleMapsLike.STATE_ANCHOR_POINT -> Log.d("bottomsheet-", "STATE_ANCHOR_POINT")
                    BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN -> Log.d("bottomsheet-", "STATE_HIDDEN")
                    BottomSheetBehaviorGoogleMapsLike.STATE_SETTLING -> Log.d("bottomsheet-", "STATE_SETTLING")
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

//        val mergedAppBarLayout = merged_appbarlayout
//        val mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout)
//        mergedAppBarLayoutBehavior.setToolbarTitle("Title Dummy")
//        mergedAppBarLayoutBehavior.setNavigationOnClickListener({ behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) })
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapClickListener { behavior.state = BottomSheetBehavior.STATE_COLLAPSED }
        setupViewModel()
    }

    private fun setupViewModel() {
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        mapViewModel.mapState.observe(this, Observer {
            when {
                it is MapState.Loading -> {
                }
                it is MapState.Success -> showPubsOnMap(it.pubs)
                it is MapState.Error -> {
                    toast(it.errorMessage)
                }
            }
        })
    }

    private fun showPubsOnMap(pubs: List<PubMapDto>) {
        (viewPager.adapter as PubPagerAdapter).items = pubs
        pubs.forEach({
            val pub = LatLng(it.map!![0].coordinates[1], it.map[0].coordinates[0])
            val marker = map.addMarker(MarkerOptions().position(pub).title(it.title))
            marker.tag = it.uniqueTag
            map.setOnMarkerClickListener(this::mapClick)
        })
    }

    private fun mapClick(m: Marker): Boolean {
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        return true
    }
}

