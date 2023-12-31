package dev.seabat.android.usbdebugswitch

import android.app.Application
import android.content.Intent
import dev.seabat.android.usbdebugswitch.services.OverlayService

class MainApplication : Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onTerminate() {
        stopService(Intent(this, OverlayService::class.java))
        super.onTerminate()
    }
}