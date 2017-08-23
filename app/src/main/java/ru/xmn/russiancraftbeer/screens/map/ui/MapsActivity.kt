package ru.xmn.russiancraftbeer.screens.map.ui

import android.app.Activity


import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import biz.laenger.android.vpbs.BottomSheetUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.pub_sheet.view.*
import org.jetbrains.anko.toast
import ru.xmn.common.extensions.*
import ru.xmn.common.widgets.ViewPagerBottomSheetBehavior
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.services.beer.PubMapDto

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LifecycleRegistryOwner {

    private lateinit var map: GoogleMap
    private lateinit var mapViewModel: MapViewModel

    private lateinit var behavior: ViewPagerBottomSheetBehavior<ViewPager>

    private val markers: MutableList<Marker> = ArrayList()

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry {
        return registry
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setupToolbar()
        setupBehaviors()
        setupMap()
        setupViewPager()
    }

    private fun setupViewPager() {
        val pubPagerAdapter = PubPagerAdapter(this,
                { s -> ViewModelProviders.of(this, PubViewModel.Factory(s.nid)).get(s.nid, PubViewModel::class.java) })
        viewPager.adapter = pubPagerAdapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {

                selectMarker(markers[position])
            }

        })
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
        behavior = ViewPagerBottomSheetBehavior.from(bottomSheet)
        BottomSheetUtils.setupViewPager(bottomSheet)
        behavior.setBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    ViewPagerBottomSheetBehavior.STATE_COLLAPSED -> Log.d("bottomsheet-", "STATE_COLLAPSED")
                    ViewPagerBottomSheetBehavior.STATE_DRAGGING -> Log.d("bottomsheet-", "STATE_DRAGGING")
                    ViewPagerBottomSheetBehavior.STATE_EXPANDED -> Log.d("bottomsheet-", "STATE_EXPANDED")
                    ViewPagerBottomSheetBehavior.STATE_HIDDEN -> Log.d("bottomsheet-", "STATE_HIDDEN")
                    ViewPagerBottomSheetBehavior.STATE_SETTLING -> Log.d("bottomsheet-", "STATE_SETTLING")
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                viewPager
                        .views
                        .map { it.pubCard }
                        .forEach({
                            performOffset(this@MapsActivity, it, slideOffset)
                            (viewPager.adapter as PubPagerAdapter).offset = slideOffset
                        })
            }
        })

        behavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapClickListener { behavior.state = ViewPagerBottomSheetBehavior.STATE_HIDDEN }
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
            markers += marker
            map.setOnMarkerClickListener(this::mapClick)
        })
    }

    private fun selectMarker(marker: Marker) {
        map.setOnCameraMoveStartedListener(null)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                marker.position.run { LatLng(this.latitude - .0025, this.longitude) },
                15f
        ), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                map.setOnCameraMoveStartedListener { behavior.state = ViewPagerBottomSheetBehavior.STATE_HIDDEN }
            }

            override fun onCancel() {
            }
        }
        )


        markers.asSequence().filter({ it !== marker }).forEach {
            it.apply {
                setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                zIndex = 0f
            }
        }
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        marker.zIndex = 1f
    }

    private fun mapClick(m: Marker): Boolean {
        selectMarker(m)
        behavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
        return true
    }
}

fun performOffset(activity: Activity, pubCardView: View, slideOffset: Float) {
    val cardMaxSize = activity.windowSize().x
    val cardMinSize = activity.resources.getDimension(R.dimen.view_pager_collapsed_width)
    val cardWidth = (offsetedValue(slideOffset, cardMinSize, cardMaxSize.toFloat())).toInt()
    pubCardView.changeWidth(cardWidth)

    val logoMaxSize = activity.resources.getDimension(R.dimen.logo_expanded_height)
    val logoMinSize = activity.resources.getDimension(R.dimen.logo_collapsed_height)
    val logoHeight = (offsetedValue(slideOffset, logoMinSize, logoMaxSize)).toInt()
    pubCardView.pubLogo.changeHeight(logoHeight)

    val contentAlpha = offsetedValue(slideOffset, 0f, 1f)
    pubCardView.pubContent.alpha = contentAlpha
    pubCardView.invalidate()
}