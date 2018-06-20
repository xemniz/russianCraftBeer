package ru.xmn.russiancraftbeer.application

import android.app.Application
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import io.realm.Realm
import io.realm.RealmConfiguration
import ru.xmn.russiancraftbeer.BuildConfig
import ru.xmn.russiancraftbeer.application.di.ApplicationComponent
import ru.xmn.russiancraftbeer.application.di.ApplicationModule
import ru.xmn.russiancraftbeer.application.di.DaggerApplicationComponent
import ru.xmn.russiancraftbeer.screens.map.ui.MapScreenReducer
import ru.xmn.russiancraftbeer.screens.map.ui.mapviewmodel.CurrentLocationModule
import zendesk.suas.Suas


class App : Application() {

    companion object {
        lateinit var component: ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate()
        initializeDagger()
        initializeRealm()
        initializeSuas()
        Fabric.with(this, Crashlytics())
    }

    private fun initializeSuas() {
        val store = Suas.createStore(MapScreenReducer())
                .withMiddleware()
                .build()
        CurrentLocationModule(context = baseContext, store = store)
    }

    private fun initializeRealm() {
        Realm.init(this)

        var configBuilder: RealmConfiguration.Builder = RealmConfiguration.Builder()
                .schemaVersion(1)

        if (BuildConfig.DEBUG)
            configBuilder = configBuilder.deleteRealmIfMigrationNeeded()

        val config = configBuilder
                .build()

        Realm.setDefaultConfiguration(config)
    }

    private fun initializeDagger() {
        component = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }
}