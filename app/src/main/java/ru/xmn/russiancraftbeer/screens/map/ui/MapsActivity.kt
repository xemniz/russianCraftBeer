package ru.xmn.russiancraftbeer.screens.map.ui

import android.Manifest
import android.app.Activity


import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import biz.laenger.android.vpbs.BottomSheetUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.pub_sheet.view.*
import org.jetbrains.anko.toast
import ru.xmn.common.extensions.*
import ru.xmn.common.widgets.ViewPagerBottomSheetBehavior
import ru.xmn.russiancraftbeer.R
import lolodev.permissionswrapper.callback.OnRequestPermissionsCallBack
import lolodev.permissionswrapper.wrapper.PermissionWrapper
import ru.xmn.russiancraftbeer.services.beer.MapPoint


class MapsActivity : AppCompatActivity(), LifecycleRegistryOwner {

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry {
        return registry
    }

    private lateinit var mapViewModel: MapViewModel
    private lateinit var behavior: ViewPagerBottomSheetBehavior<ViewPager>

    private val mapViewManager = MapViewManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        setupToolbar()
        setupBehaviors()
        setupMap()
        setupViewPager()
    }

    private fun setupMap() {
        mapViewManager.init(object : MapViewManager.Delegate {
            override fun mapClick() {
                behavior.state = ViewPagerBottomSheetBehavior.STATE_HIDDEN
            }

            override fun locationChange(l: LatLng) {
                setupViewModel(MapPoint.from(l))
            }

            override fun requestPermission() {
                requestPerm()
            }

            override fun cameraMove() {
                behavior.state = ViewPagerBottomSheetBehavior.STATE_HIDDEN
            }

            override fun markerClick(tag: String) {
                behavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
                val adapter = viewPager.adapter as PubPagerAdapter
                val i = adapter.items.indexOfFirst { tag == it.uniqueTag }
                viewPager.setCurrentItem(i, true)
            }

            override fun myPositionClick() {
                behavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
                viewPager.setCurrentItem(0, true)
            }
        })
    }

    private fun setupViewPager() {
        val pubPagerAdapter = PubPagerAdapter(
                this,
                { s -> ViewModelProviders.of(this, PubViewModel.Factory(s.nid)).get(s.nid, PubViewModel::class.java) },
                { clickOnItem(it) }
        )
        viewPager.adapter = pubPagerAdapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                mapViewManager.selectMarker(position)
            }

        })
    }

    private fun clickOnItem(position: Int) {
        val inBounds: Boolean = !mapViewManager.selectMarkerIfNotInBounds(position)
        if (inBounds && behavior.state != ViewPagerBottomSheetBehavior.STATE_EXPANDED) behavior.state = ViewPagerBottomSheetBehavior.STATE_EXPANDED
        if (behavior.state == ViewPagerBottomSheetBehavior.STATE_EXPANDED) behavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
    }

    private fun setupToolbar() {
//        val toolbar = toolbar
//        setSupportActionBar(toolbar)
//        supportActionBar?.setTitle("Beer map")
    }

    private fun setupBehaviors() {
        val bottomSheet = viewPager
        behavior = ViewPagerBottomSheetBehavior.from(bottomSheet)
        BottomSheetUtils.setupViewPager(bottomSheet)
        behavior.setBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
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

    private fun requestPerm() {
        PermissionWrapper.Builder(this)
                .addPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                .addPermissionRationale("Rationale message")
                .addPermissionsGoSettings(true)
                .addRequestPermissionsCallBack(object : OnRequestPermissionsCallBack {
                    override fun onGrant() {
                        mapViewManager.showRefreshedLocation()
                    }

                    override fun onDenied(permission: String) {
                    }
                }).build().request();
    }

    private fun setupViewModel(mapPoint: MapPoint) {
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        mapViewModel.request(mapPoint)
        mapViewModel.mapState.observe(this, Observer {
            when {
                it is MapState.Loading -> {
                    mapLoading.visibility = View.VISIBLE
                }
                it is MapState.Success -> {
                    mapLoading.visibility = View.GONE
                    (viewPager.adapter as PubPagerAdapter).items = it.pubs
                    mapViewManager.showPubsOnMap(it.pubs)
                }
                it is MapState.Error -> {
                    toast(it.errorMessage)
                }
            }
        })
    }

    override fun onBackPressed() {
        when {
            behavior.state == ViewPagerBottomSheetBehavior.STATE_EXPANDED -> behavior.state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
            behavior.state == ViewPagerBottomSheetBehavior.STATE_COLLAPSED -> behavior.state = ViewPagerBottomSheetBehavior.STATE_HIDDEN
            else -> super.onBackPressed()
        }
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