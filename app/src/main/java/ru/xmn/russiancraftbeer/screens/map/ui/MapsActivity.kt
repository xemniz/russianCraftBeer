package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.pub_sheet.*
import org.jetbrains.anko.toast
import ru.xmn.common.widgets.BottomSheetBehaviorGoogleMapsLike
import ru.xmn.common.widgets.MergedAppBarLayoutBehavior
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.services.beer.PubDto
import ru.xmn.russiancraftbeer.services.beer.PubMapDto

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LifecycleRegistryOwner {

    private lateinit var map: GoogleMap
    private lateinit var mapViewModel: MapViewModel
    private lateinit var pubViewModel: PubViewModel

    private lateinit var behavior: BottomSheetBehaviorGoogleMapsLike<NestedScrollView>

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry {
        return registry
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val toolbar = toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("Beer map")
        setupViewPager()
        setupMap()
        setupBehaviors()
    }

    private fun setupViewPager() {
        
    }

    private fun setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupBehaviors() {
        val coordinatorLayout = coordinator
        val bottomSheet = bottom_sheet
        behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet)
        behavior.addBottomSheetCallback(object : BottomSheetBehaviorGoogleMapsLike.BottomSheetCallback() {
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

        val mergedAppBarLayout = merged_appbarlayout
        val mergedAppBarLayoutBehavior = MergedAppBarLayoutBehavior.from(mergedAppBarLayout)
        mergedAppBarLayoutBehavior.setToolbarTitle("Title Dummy")
        mergedAppBarLayoutBehavior.setNavigationOnClickListener({ behavior.setState(BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED) })
        behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapClickListener { behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_HIDDEN }
        setupViewModel()
    }

    private fun setupViewModel() {
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java);
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

        pubViewModel = ViewModelProviders.of(this).get(PubViewModel::class.java)
        pubViewModel.mapState.observe(this, Observer {
            when {
                it is PubState.Loading -> {
                    behavior.setAllowDragAndScroll(false)
                    progressBarTopLayout.visibility = View.VISIBLE
                    bindPub(PubDto.empty(), "", "")
                }
                it is PubState.Success -> {
                    behavior.setAllowDragAndScroll(true)
                    progressBarTopLayout.visibility = View.GONE
                    bindPub(it.pub, it.title, it.type)
                }
                it is PubState.Error -> {
                    toast(it.errorMessage)
                }
            }
        })
    }

    private fun bindPub(pub: PubDto, title: String, type: String) {
        pubTitle.text = title
        pubType.text = type
        Glide.with(this)
                .load(pub.logo)
                .listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
//                        pubLogo.setImageDrawable(resource)
                        return true
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        return true
                    }
                })
                .preload()
        pubDescription.text = pub.body
    }

    private fun showPubsOnMap(pubs: List<PubMapDto>) {
        pubs.forEach({
            val pub = LatLng(it.map!![0].coordinates[1], it.map[0].coordinates[0])
            val marker = map.addMarker(MarkerOptions().position(pub).title(it.title))
            marker.tag = "${it.nid},${it.title},${it.type}"
            map.setOnMarkerClickListener(this::mapClick)
        })
    }

    private fun mapClick(m: Marker): Boolean {
        behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED
        val (nid, title, type) = (m.tag as String).split(",")
        pubViewModel.clickPub(nid, title, type)
        return true
    }
}

