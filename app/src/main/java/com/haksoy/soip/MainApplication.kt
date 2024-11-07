package com.haksoy.soip

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}