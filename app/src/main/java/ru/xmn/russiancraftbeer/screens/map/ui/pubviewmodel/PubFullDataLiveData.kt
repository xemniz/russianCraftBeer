package ru.xmn.russiancraftbeer.screens.map.ui.pubviewmodel

import android.arch.lifecycle.LiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import ru.xmn.russiancraftbeer.screens.map.bl.PubFullDataQuery
import ru.xmn.russiancraftbeer.screens.map.bl.data.PubFullData
import ru.xmn.common.repo.RepoSingleFactory

class PubFullDataLiveData(private val repository: RepoSingleFactory<PubFullDataQuery, PubFullData>) : LiveData<PubState>() {
    fun getPub(id: String) {
        repository(PubFullDataQuery(id))
                .toFlowable()
                .map { either -> either.fold({ PubState.Error(it) }, { PubState.Success(it) }) }
                .startWith(PubState.Loading())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ value = it })
    }
}