package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.LiveData

class CurrentPubItemLiveData : LiveData<MapState>() {
    var currentItemPosition = 0
    fun pushNewState(mapState: MapState): LiveData<MapState> {
        currentItemPosition = 0
        value = mapState
        return this
    }

    fun pushCurrentItemPosition (itemPosition: Int){
        currentItemPosition = itemPosition
        if (value is MapState.Success){
            val lastSuccessMapState = value as MapState.Success
            value = MapState.Success(lastSuccessMapState.pubs, currentItemPosition, lastSuccessMapState.listUniqueId)
        }
    }
}