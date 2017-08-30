package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase

/**
 * Created by USER on 30.08.2017.
 */
class MapPubListLiveData(val location: LatLng, val mapListUseCase: MapListUseCase) : MutableLiveData<MapState>() {
    private var subscribe: Disposable? = null

    override fun onActive() {
        subscribe = mapListUseCase.getPubsForMap(location)
                .map<MapState> { MapState.Success(it, 0) }
                .startWith(MapState.Loading())
                .onErrorReturn { MapState.Error(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ value = it })
    }

    override fun onInactive() {
        subscribe?.apply {
            if (!isDisposed)
                dispose()
        }
    }
}