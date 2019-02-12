package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils.OverlayPermissionChecker

class BootCompleteReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "BootCompleteReceiver"
        private val DEBUG = BuildConfig.DEBUG
    }

    override fun onReceive(context: Context, intent: Intent) {
        if(DEBUG) Log.d(TAG, "START RECEIVER")
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.action)) {
            // TODO: Activity ではなく Service を起動する
//            if (OverlayPermissionChecker.isEnabled(context)) {
//                val intent = Intent(context, OverlayService::class.java)
//                context.startService(intent)
//            }

            Intent(context, MainActivity::class.java).let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(it)
            }

        }
    }
}
