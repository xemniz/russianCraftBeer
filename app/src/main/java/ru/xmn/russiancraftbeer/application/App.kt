package ru.xmn.russiancraftbeer.application

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration
import ru.xmn.russiancraftbeer.BuildConfig
import ru.xmn.russiancraftbeer.application.di.ApplicationComponent
import ru.xmn.russiancraftbeer.application.di.ApplicationModule
import ru.xmn.russiancraftbeer.application.di.DaggerApplicationComponent
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric



class App : Application() {

    companion object {
        lateinit var component: ApplicationComponent
    }

    override fun onCreate() {
        super.onCreate()
        initializeDagger()
        initializeRealm()
        Fabric.with(this, Crashlytics())
    }

    private fun initializeRealm() {
        Realm.init(this)

        var configBuilder: RealmConfiguration.Builder = RealmConfiguration.Builder()
                .schemaVersion(1)

        if (BuildConfig.DEBUG)
            configBuilder = configBuilder.deleteRealmIfMigrationNeeded()

        val config = configBuilder
//                .migration(Migration())
                .build()

        Realm.setDefaultConfiguration(config)
    }

    fun initializeDagger() {
        component = DaggerApplicationComponent.builder()
                .applicationModule(ApplicationModule(this))
                .build()
    }
}
