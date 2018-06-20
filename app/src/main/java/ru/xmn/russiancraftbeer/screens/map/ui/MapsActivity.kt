package ru.xmn.russiancraftbeer.screens.map.ui


import android.Manifest


import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
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
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.MapViewModel
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.StartListenLocation
import ru.xmn.russiancraftbeer.screens.map.ui.pubviewmodel.PubViewModel

class MapsActivity : AppCompatActivity() {
    private var lastListHash: String = ""
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
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        setContentView(R.layout.activity_maps)
        map.view!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        setupBehaviors()
        val offset = savedInstanceState?.get(OFFSET) as Float? ?: 0f
        setupMap(offset)
        setupViewPager(offset)
        setupViewModel()
        setClickListeners()
    }

    private fun setClickListeners() {
        help_card_text_view.movementMethod = LinkMovementMethod.getInstance()
        help_button.setOnClickListener { help_card.visible() }
        help_ok_button.setOnClickListener { help_card.gone() }
        map_error_button.setOnClickListener { mapViewModel.store.dispatch(StartLoadingPubs) }
    }

    private fun setupMap(offset: Float) {
        mapViewManager.init(object : MapViewManager.Delegate {
            override fun mapClick() {
                mapViewModel.store.dispatch(MapClick)
            }

            override fun requestPermission() {
                requestPerm()
            }

            override fun cameraMove() {
                mapViewModel.store.dispatch(MapClick)
            }

            override fun markerClick(tag: String) {
                mapViewModel.store.dispatch(SelectItem(tag, true))
            }

            override fun myPositionClick() {
                mapViewModel.store.dispatch(SelectMyLocation)
            }
        })

        layoutOnSlide(offset)

        // Find ZoomControl view
        @SuppressLint("ResourceType")
        val zoomControls = map.view!!.findViewById<View>(0x1)

        if (zoomControls != null && zoomControls.layoutParams is RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            val params = zoomControls.layoutParams as RelativeLayout.LayoutParams

            // Align it to - parent top|left
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP)

            // Update margins, set to 10dp
            val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
                    resources.displayMetrics).toInt()
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
                val pubShortData = pubPagerAdapter.items[position]
                mapViewModel.store.dispatch(SelectItem(pubShortData.tag, false))

                try {
                    firebaseAnalytics.log(
                            FirebaseAnalytics.Event.VIEW_ITEM,
                            FirebaseAnalytics.Param.ITEM_ID to pubShortData.nid,
                            FirebaseAnalytics.Param.ITEM_NAME to pubShortData.title,
                            FirebaseAnalytics.Param.CONTENT_TYPE to pubShortData.type
                    )
                } catch (e: Exception) {
                    Log.d("MapsActivity", "Log failed")
                    e.printStackTrace()
                }

            }

        })
    }

    private fun clickOnItem() {
        val inBounds: Boolean = mapViewManager.isCurrentItemVisibleInMap()
        mapViewModel.store.dispatch(ClickOnItem(inBounds))
    }

    private fun setupBehaviors() {
        val bottomSheet = viewPager
        behavior = from(bottomSheet)
        BottomSheetUtils.setupViewPager(bottomSheet)
        behavior.setBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(sheet: View, newState: Int) {
                val bottomSheetAction = when (newState) {
                    STATE_EXPANDED -> ExpandBottomSheet
                    STATE_COLLAPSED -> CollapseBottomSheet
                    STATE_HIDDEN -> HideBottomSheet
                    else -> null
                }
                bottomSheetAction?.let {
                    mapViewModel.store.dispatch(it)
                }
            }

            override fun onSlide(sheet: View, slideOffset: Float) {
                (viewPager.adapter as PubPagerAdapter).offset = slideOffset

                layoutOnSlide(slideOffset)

                viewPager
                        .views
                        .map { it.pubCard }
                        .forEach {
                            performOffset(this@MapsActivity, it, slideOffset)
                        }
            }
        })
    }

    private fun layoutOnSlide(slideOffset: Float) {
        val contentAlpha = offsetedValue(slideOffset, 1f, 0f)
        map.view?.alpha = contentAlpha
    }

    private fun requestPerm() {
        if (hasLocationPermission()) {
            mapViewManager.onPermissionGranted()
            mapViewModel.store.dispatch(StartListenLocation)
        }

        PermissionWrapper.Builder(this)
                .addPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                .addPermissionRationale("Rationale message")
                .addPermissionsGoSettings(true)
                .addRequestPermissionsCallBack(object : OnRequestPermissionsCallBack {
                    override fun onGrant() {
                        mapViewManager.onPermissionGranted()
                        mapViewModel.store.dispatch(StartListenLocation)
                    }

                    override fun onDenied(permission: String) {
                    }
                }).build().request()
    }

    private fun setupViewModel() {
        mapViewModel = ViewModelProviders.of(this).get(MapViewModel::class.java)
        mapViewModel.mapScreen.observe(this, Observer {
            it?.let(this@MapsActivity::onNewScreenState)
        })
    }

    private fun onNewScreenState(mapScreenState: MapScreenState) {
        bindViewPager(mapScreenState)
        mapViewManager.bindMap(mapScreenState)
        bindLoading(mapScreenState)
        bindBehaviorState(mapScreenState.bottomSheetState)
        if (mapScreenState.goBack) super.onBackPressed()
    }

    private fun bindBehaviorState(bottomSheetState: BottomSheetState) {
        when (bottomSheetState) {
            BottomSheetState.Collapsed -> {
                if (behavior.state != STATE_COLLAPSED)
                    behavior.state = STATE_COLLAPSED
            }
            BottomSheetState.Expanded -> {
                if (behavior.state != STATE_EXPANDED)
                    behavior.state = STATE_EXPANDED
            }
            BottomSheetState.Hidden -> {
                if (behavior.state != STATE_HIDDEN)
                    behavior.state = STATE_HIDDEN
            }
        }
    }

    private fun bindViewPager(mapScreenState: MapScreenState) {
        when (mapScreenState.pubsState) {
            is PubsState.Loading -> {
            }
            is PubsState.Success -> {
                if (mapScreenState.pubsState.listHash != lastListHash) {
                    (viewPager.adapter as PubPagerAdapter).items = mapScreenState.pubsState.pubs
                    lastListHash = mapScreenState.pubsState.listHash
                }
                val indexOfSelected = mapScreenState.indexOfSelected()
                if (viewPager.currentItem != indexOfSelected)
                    viewPager.setCurrentItem(indexOfSelected ?: 0, true)
            }
            is PubsState.Error -> {
                (viewPager.adapter as PubPagerAdapter).items = emptyList()
            }
        }
    }

    private fun bindLoading(mapScreenState: MapScreenState) {
        when (mapScreenState.pubsState) {
            is PubsState.Loading -> {
                mapError.gone()
                mapLoading.visible()
            }
            is PubsState.Success -> {
                mapError.gone()
                mapLoading.gone()
            }
            is PubsState.Error -> {
                Crashlytics.logException(mapScreenState.pubsState.e)
                mapLoading.gone()
                mapError.visible()
            }
        }
    }

    override fun onBackPressed() {
        mapViewModel.store.dispatch(BackPressed)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat(OFFSET, (viewPager.adapter as PubPagerAdapter).offset)
    }

    companion object {
        private const val OFFSET: String = "OFFSET"
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
    pubCardView.pubLogoBack.changeHeight(logoHeight)

    val contentAlpha = offsetedValue(slideOffset, 0f, 1f)
    pubCardView.pubContacts.alpha = contentAlpha
    pubCardView.pubDescription.alpha = contentAlpha
    pubCardView.progressBarTopLayout.alpha = contentAlpha
    pubCardView.pub_error_text.alpha = contentAlpha
    pubCardView.pub_error_button.alpha = contentAlpha

    val helpButtonAlpha = offsetedValue(slideOffset, 1f, 0f)
    activity.help_button.alpha = helpButtonAlpha

    pubCardView.invalidate()

    val rotation = offsetedValue(slideOffset, 0F, -180F)
    pubCardView.pubSlideIcon.rotation = rotation

    val elevation = offsetedValue(slideOffset, 24.px.toFloat(), 0.toFloat())
    pubCardView.headCard.radius = Math.max(elevation, 0.5f)
}