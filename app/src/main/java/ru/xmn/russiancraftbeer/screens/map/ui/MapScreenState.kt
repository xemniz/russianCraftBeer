package ru.xmn.russiancraftbeer.screens.map.ui

import ru.xmn.common.extensions.printFirst
import ru.xmn.russiancraftbeer.screens.map.bl.data.MapPoint
import ru.xmn.russiancraftbeer.screens.map.bl.data.Pubs
import ru.xmn.russiancraftbeer.screens.map.bl.data.sortedByClosest
import java.util.*

data class MapScreenState(
        val pubsState: PubsState = PubsState.Loading,
        val userLocation: MapPoint = MapPoint.moscow(),
        val bottomSheetState: BottomSheetState = BottomSheetState.Collapsed,
        val helpCardShowed: Boolean = false,
        val itemTagToSelect: String? = null,
        val focus: MapPoint = MapPoint.moscow(),
        val goBack: Boolean = false) {
    fun indexOfSelected(): Int? {
        val indexOfFirst = (pubsState as? PubsState.Success)?.pubs?.indexOfFirst { it.tag == itemTagToSelect }
                ?: -1
        return if (indexOfFirst < 0) null else indexOfFirst
    }
}

sealed class PubsState {
    data class Success(
            val pubs: Pubs = emptyList(),
            val listHash: String = generateHash()) : PubsState() {
        fun transformForPoint(mapPoint: MapPoint) = PubsState.Success(pubs.sortedByClosest(mapPoint))
        override fun toString() = "Success(pubs=${pubs.printFirst(5) { it.title }}, listHash='$listHash')"
    }

    data class Error(val e: Throwable) : PubsState() {
        init {
            e.printStackTrace()
        }
    }

    object Loading : PubsState()

    companion object {
        fun generateHash() = UUID.randomUUID().toString()
    }
}

sealed class BottomSheetState {
    object Expanded : BottomSheetState()
    object Collapsed : BottomSheetState()
    object Hidden : BottomSheetState()
}