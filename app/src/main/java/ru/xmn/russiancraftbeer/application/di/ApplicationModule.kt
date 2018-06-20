package ru.xmn.russiancraftbeer.application.di

import android.content.Context
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import ru.xmn.russiancraftbeer.application.App
import ru.xmn.russiancraftbeer.screens.map.bl.MapListUseCase
import ru.xmn.russiancraftbeer.screens.map.ui.*
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.CurrentLocationModule
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.LocationOwnerReducer
import zendesk.suas.*
import javax.inject.Singleton

@Module()
class ApplicationModule(private val app: App) {
    @Provides
    @Singleton
    fun provideApplicationContext(): Context = app

    @Provides
    @Singleton
    fun provideStore(mapListUseCase: MapListUseCase, baseContext: Context): Store {
        val loggerMiddleware = LoggerMiddleware.Builder()
                .withLineLength(160)
                .withActionTransformer(Action<*>::toString)
                .withStateTransformer(State::toString)
                .build()
        val store = Suas
                .createStore(MapScreenReducer(), LocationOwnerReducer())
                .withMiddleware(MapPubsMiddleware(mapListUseCase), loggerMiddleware)
                .withDefaultFilter(Filters.EQUALS)
                .build()
        CurrentLocationModule(context = baseContext, store = store)
        return store
    }
}

class MapPubsMiddleware(private val mapListUseCase: MapListUseCase) : Middleware {
    override fun onAction(action: Action<*>, state: GetState, dispatcher: Dispatcher, continuation: Continuation) {
        if (action === StartLoadingPubs) {
            mapListUseCase.getPubsForMap()
                    .map<MapScreenAction<*>> {
                        it.fold({ PubsLoadingError(it) }, { PubsLoaded(it) })
                    }
                    .subscribeOn(Schedulers.io())
                    .subscribe({ dispatcher.dispatch(it) }, { dispatcher.dispatch(PubsLoadingError(it)) })
        }
        continuation.next(action)
    }
}
