package ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel

import android.arch.lifecycle.LiveData

class CurrentPubItemLiveData : LiveData<MapState>() {
    var currentItemPosition = 0
    fun pushNewState(mapState: MapState): LiveData<MapState> {
        currentItemPosition = 0
        value = mapState
        return this
    }

    fun pushCurrentItemPosition (itemPosition: Int, focus: Focus = Focus.ON_ITEM){
        currentItemPosition = itemPosition
        if (value is MapState.Success){
            val lastSuccessMapState = value as MapState.Success
            value = MapState.Success(lastSuccessMapState.pubs, currentItemPosition, lastSuccessMapState.listUniqueId, focus)
        }
    }
}

enum class Focus {
    ON_ITEM, ON_MY_LOCATION
}
