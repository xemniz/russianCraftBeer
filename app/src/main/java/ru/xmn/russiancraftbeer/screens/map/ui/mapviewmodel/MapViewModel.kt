package ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import ru.xmn.russiancraftbeer.screens.map.ui.*
import zendesk.suas.Store
import javax.inject.Inject
import kotlin.properties.Delegates

class MapViewModel : ViewModel() {
    @Inject
    lateinit var store: Store
    val mapScreen: MutableLiveData<MapScreenState> = MutableLiveData()
    init {
        App.component.inject(this)
        store.addListener(MapScreenState::class.java) {
            mapScreen.value = it
        }
        store.dispatch(StartLoadingPubs)
    }
}