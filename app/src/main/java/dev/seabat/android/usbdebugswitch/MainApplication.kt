package dev.seabat.android.usbdebugswitch

import android.app.Application

class MainApplication : Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}