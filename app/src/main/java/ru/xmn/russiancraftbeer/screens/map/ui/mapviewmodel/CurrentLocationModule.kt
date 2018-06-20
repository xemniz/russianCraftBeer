package ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import ru.xmn.common.extensions.distanceTo
import ru.xmn.russiancraftbeer.screens.map.bl.data.MapPoint
import ru.xmn.russiancraftbeer.screens.map.ui.GotNewLocation
import ru.xmn.russiancraftbeer.screens.map.ui.MapScreenAction
import zendesk.suas.Action
import zendesk.suas.Reducer
import zendesk.suas.Store
import kotlin.properties.Delegates

class CurrentLocationModule(val context: Context, store: Store) {
    private var currentState: LocationOwnerState = LocationOwnerState.DontListenLocation

    private var currentLocation: MapPoint by Delegates.observable(MapPoint.moscow()) { _, _, newValue ->
        store.dispatch(GotNewLocation(mapPoint = newValue))
    }

    private val locationListener: LocationListener = LocationListener(context) {
        val newLocation = LatLng(it.latitude, it.longitude)
        if (newLocation.distanceTo(currentLocation.latLng()) > 1000) {
            currentLocation = MapPoint.from(newLocation)
        }
    }

    init {
        store.addListener(LocationListenerModuleState::class.java) {
            when (it.ownerState) {
                LocationOwnerState.ListenLocation -> {
                    if (currentState != it.ownerState) {
                        locationListener.startListening()
                        currentState = it.ownerState
                    }
                }
                LocationOwnerState.DontListenLocation -> {
                    if (currentState != it.ownerState) {
                        locationListener.stopListening()
                        currentState = it.ownerState
                    }
                }
            }
        }
    }
}

data class LocationListenerModuleState(val ownerState: LocationOwnerState)

sealed class LocationOwnerState {
    object ListenLocation : LocationOwnerState()
    object DontListenLocation : LocationOwnerState()
}

sealed class LocationOwnerAction<T>(action: String, value: T) : Action<T>(action, value)
object StopListenLocation : LocationOwnerAction<Unit?>("StopListenLocation", null)
object StartListenLocation : LocationOwnerAction<Unit?>("StartListenLocation", null)

class LocationOwnerReducer : Reducer<LocationListenerModuleState>() {
    override fun getInitialState(): LocationListenerModuleState {
        return LocationListenerModuleState(LocationOwnerState.DontListenLocation)
    }

    override fun reduce(state: LocationListenerModuleState, action: Action<*>): LocationListenerModuleState? {
        val locationOwnerAction = (action as? LocationOwnerAction<*>) ?: return state
        return when (locationOwnerAction) {
            StartListenLocation -> state.copy(ownerState = LocationOwnerState.ListenLocation)
            StopListenLocation -> state.copy(ownerState = LocationOwnerState.DontListenLocation)
        }
    }
}