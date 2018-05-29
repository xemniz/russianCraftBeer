package ru.xmn.russiancraftbeer.screens.map.ui.pubviewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.di.MapModule
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubFullData
import javax.inject.Inject

class PubViewModel(val nid: String) : ViewModel() {
    @Inject
    lateinit var mapState: PubFullDataLiveData

    init {
        App.component.provideMapComponentBuilder.mapModule(MapModule()).build().inject(this)

        refresh()
    }

    fun refresh() {
        clickPub(nid)
    }

    private fun clickPub(id: String) {
        mapState.getPub(id)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(val nid: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PubViewModel(nid) as T
        }
    }
}

sealed class PubState {
    class Success(val pub: PubFullData) : PubState()
    class Error(val e: Throwable) : PubState() {
        val errorMessage: String

        init {
            e.printStackTrace()
            errorMessage = "Something went wrong"
        }
    }

    class Loading : PubState()
}