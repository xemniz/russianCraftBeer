package ru.xmn.russiancraftbeer.screens.map.ui

import ru.xmn.common.extensions.printFirst
import ru.xmn.russiancraftbeer.screens.map.bl.data.*
import zendesk.suas.Action
import zendesk.suas.Reducer


sealed class MapScreenAction<T>(action: String, value: T) : Action<T>(action, value)
data class GotNewLocation(val mapPoint: MapPoint) : MapScreenAction<MapPoint>("GotNewLocation", mapPoint)
data class ClickOnItem(val inBounds: Boolean) : MapScreenAction<Boolean>("ClickOnItem", inBounds)
object StartLoadingPubs : MapScreenAction<Unit?>("StartLoadingPubs", null)
object ExpandBottomSheet : MapScreenAction<Unit?>("ExpandBottomSheet", null)
object CollapseBottomSheet : MapScreenAction<Unit?>("CollapseBottomSheet", null)
object HideBottomSheet : MapScreenAction<Unit?>("HideBottomSheet", null)
object SelectMyLocation : MapScreenAction<Unit?>("SelectMyLocation", null)
object MapClick : MapScreenAction<Unit?>("MapClick", null)
object BackPressed : MapScreenAction<Unit?>("BackPressed", null)
data class PubsLoaded(val pubs: Pubs) : MapScreenAction<Pubs>("PubsLoaded", pubs) {
    override fun toString(): String {
        return "PubsLoaded(pubs=${pubs.printFirst(5) { it.title }})"
    }
}

data class PubsLoadingError(val error: Throwable) : MapScreenAction<Throwable>("PubsLoadingError", error)
data class SelectItem(val tag: String?, val fromMapClick: Boolean) : MapScreenAction<String?>("SelectItem", tag)

class MapScreenReducer : Reducer<MapScreenState>() {
    override fun getInitialState() = MapScreenState()

    override fun reduce(state: MapScreenState, action: Action<*>): MapScreenState? {
        val mapScreenAction = (action as? MapScreenAction<*>) ?: return state

        return when (mapScreenAction) {
            is StartLoadingPubs -> {
                state.copy(pubsState = PubsState.Loading)
            }
            is ExpandBottomSheet -> {
                state.copy(bottomSheetState = BottomSheetState.Expanded)
            }
            is CollapseBottomSheet -> {
                state.copy(bottomSheetState = BottomSheetState.Collapsed)
            }
            is HideBottomSheet -> {
                state.copy(bottomSheetState = BottomSheetState.Hidden)
            }
            is PubsLoaded -> {
                val pubs = mapScreenAction.pubs.sortedByClosest(state.userLocation)
                state.copy(pubsState = PubsState.Success(pubs), focus = pubs.initialFocus
                        ?: state.userLocation, itemTagToSelect = pubs.initialTag)
            }
            is PubsLoadingError -> {
                state.copy(pubsState = PubsState.Error(mapScreenAction.error))
            }
            is SelectItem -> {
                val success = (state.pubsState as? PubsState.Success)
                val newFocus = (success?.pubs
                        ?.firstWithTag(mapScreenAction.tag)
                        ?.mapPoint
                        ?: state.focus)
                val bottomSheetState =
                        if (mapScreenAction.fromMapClick) BottomSheetState.Collapsed
                        else state.bottomSheetState
                state.copy(
                        itemTagToSelect = mapScreenAction.tag,
                        focus = newFocus,
                        bottomSheetState = bottomSheetState)
            }
            is GotNewLocation -> {
                val success = (state.pubsState as? PubsState.Success)
                        ?: return state.copy(userLocation = mapScreenAction.mapPoint)

                val newPubsState = success.transformForPoint(mapScreenAction.mapPoint)
                state.copy(
                        userLocation = mapScreenAction.mapPoint,
                        pubsState = newPubsState,
                        itemTagToSelect = newPubsState.pubs.initialTag,
                        focus = newPubsState.pubs.initialFocus ?: state.userLocation)
            }
            SelectMyLocation -> {
                state.copy(
                        itemTagToSelect = null,
                        focus = state.userLocation,
                        bottomSheetState = BottomSheetState.Collapsed)
            }
            BackPressed -> {
                when (state.bottomSheetState) {
                    BottomSheetState.Expanded -> state.copy(bottomSheetState = BottomSheetState.Collapsed)
                    BottomSheetState.Collapsed -> state.copy(bottomSheetState = BottomSheetState.Hidden)
                    BottomSheetState.Hidden -> state.copy(goBack = true)
                }
            }
            MapClick -> {
                state.copy(bottomSheetState = BottomSheetState.Hidden, helpCardShowed = false)
            }
            is ClickOnItem -> {
                when {
                    !mapScreenAction.inBounds -> {
                        val newFocus = (state.pubsState as? PubsState.Success)?.pubs?.firstWithTag(state.itemTagToSelect)?.mapPoint
                                ?: state.userLocation
                        state.copy(focus = newFocus)
                    }
                    mapScreenAction.inBounds && state.bottomSheetState != BottomSheetState.Expanded -> state.copy(bottomSheetState = BottomSheetState.Expanded)
                    state.bottomSheetState == BottomSheetState.Expanded -> state.copy(bottomSheetState = BottomSheetState.Collapsed)
                    else -> state
                }
            }
        }
    }
}