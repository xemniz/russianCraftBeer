package ru.xmn.russiancraftbeer.screens.map.ui


import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import biz.laenger.android.vpbs.BottomSheetUtils
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.pub_sheet.view.*
import lolodev.permissionswrapper.callback.OnRequestPermissionsCallBack
import lolodev.permissionswrapper.wrapper.PermissionWrapper
import org.jetbrains.anko.toast
import ru.xmn.common.extensions.*
import ru.xmn.common.widgets.ViewPagerBottomSheetBehavior
import ru.xmn.common.widgets.ViewPagerBottomSheetBehavior.*
import ru.xmn.russiancraftbeer.R


class MapsActivity : AppCompatActivity(), LifecycleRegistryOwner {
    private val OFFSET: String = "OFFSET"

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry {
        return registry
    }
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var mapViewModel: MapViewModel
    private lateinit var behavior: ViewPagerBottomSheetBehavior<ViewPager>

    private val mapViewManager = MapViewManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_maps)
        map.view!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        setupToolbar()
        setupBehaviors()
        setupMap(savedInstanceState?.get(OFFSET) as Float? ?:0f)
        setupViewPager(savedInstanceState?.get(OFFSET) as Float? ?:0f)
        setupViewModel()
    }

    private fun setupMap(offset: Float) {
        mapViewManager.init(object : MapViewManager.Delegate {
            override fun mapClick() {
                behavior.state = STATE_HIDDEN
            }

            override fun requestPermission() {
                requestPerm()
            }

            override fun cameraMove() {
                behavior.state = STATE_HIDDEN
            }

            override fun markerClick(tag: String) {
                behavior.state = STATE_COLLAPSED
                val adapter = viewPager.adapter as PubPagerAdapter
                val i = adapter.items.indexOfFirst { tag == it.uniqueTag }
                viewPager.setCurrentItem(i, true)
            }

            override fun myPositionClick() {
                behavior.state = STATE_COLLAPSED
                viewPager.setCurrentItem(0, true)
            }
        })

        changMapAlpha(offset)

        // Find ZoomControl view
        @SuppressLint("ResourceType \"type\"")
        val zoomControls = map.view!!.findViewById<View>(0x1)

        if (zoomControls != null && zoomControls.getLayoutParams() is RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            val params = zoomControls.getLayoutParams() as RelativeLayout.LayoutParams

            // Align it to - parent top|left
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
//            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT)

            // Update margins, set to 10dp
            val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
                    getResources().getDisplayMetrics()).toInt()
            params.setMargins(margin, 64.px, margin, margin)
        }
    }

    private fun setupViewPager(offset: Float) {
        val pubPagerAdapter = PubPagerAdapter(
                this,
                { s -> ViewModelProviders.of(this, PubViewModel.Factory(s.nid)).get(s.nid, PubViewModel::class.java) },
                { clickOnItem(it) }
        )
        pubPagerAdapter.offset = offset
        viewPager.adapter = pubPagerAdapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                mapViewModel.currentItemPosition = position
                mapViewManager.pushMarkerPosition(position)
            }

        })
    }

    private fun clickOnItem(position: Int) {
        val inBounds: Boolean = mapViewManager.isMarkerInBounds(position)
        when {
            !inBounds -> mapViewManager.pushMarkerPosition(position)
            inBounds && behavior.state != STATE_EXPANDED -> behavior.state = STATE_EXPANDED
            behavior.state == STATE_EXPANDED -> behavior.state = STATE_COLLAPSED
        }
    }

    private fun setupToolbar() {
//        val toolbar = toolbar
//        setSupportActionBar(toolbar)
//        supportActionBar?.setTitle("Beer map")
    }

    private fun setupBehaviors() {
        val bottomSheet = viewPager
        behavior = from(bottomSheet)
        BottomSheetUtils.setupViewPager(bottomSheet)
        behavior.setBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(sheet: View, newState: Int) {
                println(newState)
            }

            override fun onSlide(sheet: View, slideOffset: Float) {
                (viewPager.adapter as PubPagerAdapter).offset = slideOffset

                changMapAlpha(slideOffset)

                viewPager
                        .views
                        .map { it.pubCard }
                        .forEach({
                            performOffset(this@MapsActivity, it, slideOffset)
                        })
            }
        })
        behavior.state = STATE_COLLAPSED
    }

    private fun changMapAlpha(slideOffset: Float) {
        val contentAlpha = offsetedValue(slideOffset, 1f, 0f)
        map.view?.alpha = contentAlpha
    }

    private fun requestPerm() {
        val hasPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            mapViewManager.onPermissionGranted()
            mapViewModel.onPermissionGranted()
        }

        PermissionWrapper.Builder(this)
                .addPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                .addPermissionRationale("Rationale message")
                .addPermissionsGoSettings(true)
                .addRequestPermissionsCallBack(object : OnRequestPermissionsCallBack {
                    override fun onGrant() {
                        mapViewManager.onPermissionGranted()
                        mapViewModel.onPermissionGranted()
                    }

                    override fun onDenied(permission: String) {
                    }
                }).build().request();
    }

    private fun setupViewModel() {
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        mapViewModel.mapState.observe(this, Observer {
            when {
                it is MapState.Loading -> {
                    mapLoading.visibility = View.VISIBLE
                }
                it is MapState.Success -> {
                    mapLoading.visibility = View.GONE
                    (viewPager.adapter as PubPagerAdapter).items = it.pubs
                    mapViewManager.pushItems(it.pubs, it.currentItemPosition)
                }
                it is MapState.Error -> {
                    toast(it.errorMessage)
                }
            }
        })
    }

    override fun onBackPressed() {
        when {
            behavior.state == STATE_EXPANDED -> behavior.state = STATE_COLLAPSED
            behavior.state == STATE_COLLAPSED -> behavior.state = STATE_HIDDEN
            else -> super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat(OFFSET, (viewPager.adapter as PubPagerAdapter).offset)
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
    pubCardView.pubContacts.alpha = contentAlpha
    pubCardView.pubDescription.alpha = contentAlpha
    pubCardView.progressBarTopLayout.alpha = contentAlpha

    pubCardView.invalidate()

    val rotation = offsetedValue(slideOffset, 0F, -180F)
    pubCardView.pubSlideIcon.rotation = rotation
}