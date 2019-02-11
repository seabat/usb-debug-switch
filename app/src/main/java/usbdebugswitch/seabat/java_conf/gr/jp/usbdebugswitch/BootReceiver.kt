package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "BootReceiver"
        private val DEBUG = BuildConfig.DEBUG
    }

    override fun onReceive(context: Context, intent: Intent) {
        if(DEBUG) Log.d(TAG, "START RECEIVER")
    }
}
