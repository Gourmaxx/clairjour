package com.clairjour.app

import android.app.Application
import android.util.Log
import com.clairjour.app.data.AppContainer
import com.clairjour.app.data.DefaultAppContainer
import com.clairjour.app.notifications.NotificationChannels

class ClairjourApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        try {
            container = DefaultAppContainer(this)
            NotificationChannels.register(this)
        } catch (t: Throwable) {
            Log.e("Clairjour", "Fatal init error", t)
            throw t
        }
    }
}
