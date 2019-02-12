package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager

object BootReceiverSwitch {

    /**
     * @param
     * context: アプリケーション context
     */
    fun on(appContext: Context) {
        switch(appContext, true)
    }

    /**
     * @param
     * context: アプリケーション context
     */
    fun off(appContext: Context) {
        switch(appContext, false)
    }

    fun switch(appContext: Context, enabled: Boolean) {
        var pkgName = appContext.packageName
        var pkgInfo = appContext.packageManager?.getPackageInfo(pkgName, PackageManager.GET_RECEIVERS)
        var receiverArray: ArrayList<Array<ActivityInfo>?> = arrayListOf(pkgInfo?.receivers)
        for ( receiver in receiverArray) {
            if (receiver?.get(0)?.name == "usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.BootCompleteReceiver") {
                receiver?.get(0)?.enabled = enabled
            }
        }
    }
}