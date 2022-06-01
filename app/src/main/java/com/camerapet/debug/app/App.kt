package com.camerapet.debug.app

import android.app.Application
import com.github.anrwatchdog.ANRWatchDog
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ANRWatchDog().start()
    }
}