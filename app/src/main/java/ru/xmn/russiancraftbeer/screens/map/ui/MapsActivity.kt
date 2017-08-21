package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_maps.*
import org.jetbrains.anko.toast
import ru.xmn.common.widgets.BottomSheetBehaviorGoogleMapsLike
import ru.xmn.common.widgets.MergedAppBarLayoutBehavior
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.services.beer.PubDto
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import ru.xmn.russiancraftbeer.screens.map.bl.PubUseCase
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import javax.inject.Inject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LifecycleRegistryOwner {

    private lateinit var map: GoogleMap
    private lateinit var mapViewModel: MapViewModel
    private lateinit var pubViewModel: PubViewModel

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
        setupMap()
    }

    private fun setupMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupBehaviors()
    }

    private fun setupBehaviors() {
        val coordinatorLayout = coordinator
        val bottomSheet = bottom_sheet
        val behavior = BottomSheetBehaviorGoogleMapsLike.from(bottomSheet)
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
        behavior.state = BottomSheetBehaviorGoogleMapsLike.STATE_COLLAPSED
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
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
                }
                it is PubState.Success -> bindPub(it.pub)
                it is PubState.Error -> {
                    toast(it.errorMessage)
                }
            }
        })
    }

    private fun bindPub(pub: PubDto) {

    }

    private fun showPubsOnMap(pubs: List<PubMapDto>) {
        pubs.forEach({
            val pub = LatLng(it.map!![0].coordinates[1], it.map[0].coordinates[0])
            val marker = map.addMarker(MarkerOptions().position(pub).title(it.title))
            marker.tag = it.nid
            map.setOnMarkerClickListener { m -> pubViewModel.clickPub(m.tag as String); true }
        })
    }
}

class PubViewModel : ViewModel() {
    @Inject
    lateinit var pubUseCase: PubUseCase
    val mapState: MutableLiveData<PubState> = MutableLiveData()

    init {
        App.component.provideMapComponentBuilder.mapModule(MapModule()).build().inject(this)
    }

    fun clickPub(id: String) {
        pubUseCase.getPub(id)
                .map<PubState> { PubState.Success(it) }
                .startWith(PubState.Loading())
                .onErrorReturn { PubState.Error(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mapState.value = it })
    }
}

sealed class PubState {
    class Success(val pub: PubDto) : PubState()
    class Error(private val e: Throwable) : PubState() {
        val errorMessage: String

        init {
            errorMessage = "Something went wrong"
        }
    }

    class Loading : PubState()
}

class MapViewModel : ViewModel() {
    @Inject
    lateinit var mapListUseCase: MapListUseCase
    val mapState: MutableLiveData<MapState> = MutableLiveData()

    init {
        App.component.provideMapComponentBuilder.mapModule(MapModule()).build().inject(this)
        mapListUseCase.getPabsForMap()
                .map<MapState> { MapState.Success(it) }
                .startWith(MapState.Loading())
                .onErrorReturn { MapState.Error(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ mapState.value = it })
    }
}

sealed class MapState {
    class Success(val pubs: List<PubMapDto>) : MapState()
    class Error(private val e: Throwable) : MapState() {
        val errorMessage: String

        init {
            errorMessage = "Something went wrong"
        }
    }

    class Loading : MapState()
}

