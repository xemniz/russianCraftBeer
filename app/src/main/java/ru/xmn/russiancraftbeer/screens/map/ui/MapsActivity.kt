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
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import biz.laenger.android.vpbs.BottomSheetUtils
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.pub_sheet.view.*
import lolodev.permissionswrapper.callback.OnRequestPermissionsCallBack
import lolodev.permissionswrapper.wrapper.PermissionWrapper
import ru.xmn.common.extensions.*
import ru.xmn.common.widgets.ViewPagerBottomSheetBehavior
import ru.xmn.common.widgets.ViewPagerBottomSheetBehavior.*
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.screens.map.ui.map.MapViewManager
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.MapState
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.MapViewModel
import ru.xmn.russiancraftbeer.screens.map.ui.pubviewmodel.PubViewModel


class MapsActivity : AppCompatActivity(), LifecycleRegistryOwner {
    private val OFFSET: String = "OFFSET"

    private val registry = LifecycleRegistry(this)

    override fun getLifecycle(): LifecycleRegistry {
        return registry
    }

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var mapViewModel: MapViewModel
    private lateinit var behavior: ViewPagerBottomSheetBehavior<ViewPager>

    val mapViewManager = MapViewManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        setContentView(R.layout.activity_maps)
        map.view!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        setupToolbar()
        setupBehaviors()
        setupMap(savedInstanceState?.get(OFFSET) as Float? ?: 0f)
        setupViewPager(savedInstanceState?.get(OFFSET) as Float? ?: 0f)
        setupViewModel()
        setClickListeners()
    }

    private fun setClickListeners() {
        help_button.setOnClickListener { help_card.visible() }
        help_ok_button.setOnClickListener { help_card.gone() }
        map_error_button.setOnClickListener { mapViewModel.refresh() }
    }

    private fun setupMap(offset: Float) {
        mapViewManager.init(object : MapViewManager.Delegate {
            override fun mapClick() {
                behavior.state = STATE_HIDDEN
                help_card.gone()
            }

            override fun requestPermission() {
                requestPerm()
            }

            override fun cameraMove() {
                behavior.state = STATE_HIDDEN
                help_card.gone()
            }

            override fun markerClick(tag: String) {
                behavior.state = STATE_COLLAPSED
                val adapter = viewPager.adapter as PubPagerAdapter
                val i = adapter.items.indexOfFirst { tag == it.uniqueTag }
                mapViewModel.currentItemPosition = i
            }

            override fun myPositionClick() {
                behavior.state = STATE_COLLAPSED
                mapViewModel.selectMyLocation()
            }
        })

        layoutOnSlide(offset)

        // Find ZoomControl view
        @SuppressLint("ResourceType")
        val zoomControls = map.view!!.findViewById<View>(0x1)

        if (zoomControls != null && zoomControls.getLayoutParams() is RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            val params = zoomControls.getLayoutParams() as RelativeLayout.LayoutParams

            // Align it to - parent top|left
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP)

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
                { clickOnItem() }
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

                try {
                    firebaseAnalytics.log(
                            FirebaseAnalytics.Event.VIEW_ITEM,
                            FirebaseAnalytics.Param.ITEM_ID to pubPagerAdapter.items[position].nid,
                            FirebaseAnalytics.Param.ITEM_NAME to pubPagerAdapter.items[position].title!!,
                            FirebaseAnalytics.Param.CONTENT_TYPE to pubPagerAdapter.items[position].type!!
                    )
                } catch(e: Exception) {
                    Log.d("MapsActivity", "Log failed")
                    e.printStackTrace()
                }

            }

        })
    }

    private fun clickOnItem() {
        val inBounds: Boolean = mapViewManager.isCurrentItemVisibleInMap()
        when {
            !inBounds -> mapViewManager.animateToCurrentItem()
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
                if (newState == STATE_EXPANDED) help_button.invisible()
                else help_button.visible()
            }

            override fun onSlide(sheet: View, slideOffset: Float) {
                (viewPager.adapter as PubPagerAdapter).offset = slideOffset

                layoutOnSlide(slideOffset)

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

    private fun layoutOnSlide(slideOffset: Float) {
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
            updateViewPager(it!!)
            mapViewManager.updateMap(it)
            when {
                it is MapState.Loading -> {
                    mapError.gone()
                    mapLoading.visible()
                }
                it is MapState.Success -> {
                    mapError.gone()
                    mapLoading.gone()


                }
                it is MapState.Error -> {
                    Crashlytics.logException(it.e);
                    mapLoading.gone()
                    mapError.visible()
                }
            }
        })
    }

    private var listUniqueId: String = ""

    private fun updateViewPager(mapState: MapState) {
        when {
            mapState is MapState.Loading -> {
            }
            mapState is MapState.Success -> {
                if (mapState.listUniqueId != listUniqueId) {
                    (viewPager.adapter as PubPagerAdapter).items = mapState.pubs
                    listUniqueId = mapState.listUniqueId
                }
                if (viewPager.currentItem != mapState.itemNumberToSelect)
                    viewPager.setCurrentItem(mapState.itemNumberToSelect, true)
            }
            mapState is MapState.Error -> {
                (viewPager.adapter as PubPagerAdapter).items = emptyList()
            }
        }
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
    pubCardView.pub_error_text.alpha = contentAlpha
    pubCardView.pub_error_button.alpha = contentAlpha

    pubCardView.invalidate()

    val rotation = offsetedValue(slideOffset, 0F, -180F)
    pubCardView.pubSlideIcon.rotation = rotation
}