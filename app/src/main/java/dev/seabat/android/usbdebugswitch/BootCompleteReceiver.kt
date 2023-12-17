package dev.seabat.android.usbdebugswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.seabat.android.usbdebugswitch.utils.OverlayPermissionChecker

class BootCompleteReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "BootCompleteReceiver"
        private val DEBUG = BuildConfig.DEBUG
    }

    private val receiverScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onReceive(context: Context, intent: Intent) {
        if(DEBUG) Log.d(TAG, "START RECEIVER")
        if(!Intent.ACTION_BOOT_COMPLETED.equals(intent.action)) {
            return
        }

        if (!OverlayPermissionChecker.isEnabled(context)) {
            return
        }

        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val offString = context.getString(R.string.setting_overlay_off)

                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                    val statusString = sharedPref.getString("pref_setting_overlay", offString)
                    if(DEBUG) Log.d(TAG, "overlay status = $statusString")
                    if (offString == statusString) {
                        return@withContext
                    }

                    if(DEBUG) Log.d(TAG, "start OverlayService")
                    val intent = Intent(context, OverlayService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                        // Android O からはバックグラウンド状態のアプリからバックグラウンドサービスを起動できない
                    } else {
                        context.startService(intent);
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
