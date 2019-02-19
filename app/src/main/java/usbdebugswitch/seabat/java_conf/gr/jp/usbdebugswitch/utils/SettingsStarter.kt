package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils

import android.app.Activity
import android.content.*
import android.provider.Settings
import android.support.v4.app.Fragment
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.MainFragment

object SettingsStarter {
    fun startForResultFromFragment(frg: Fragment) {
        startForResult(frg)
    }

    fun startForResultFromActivity(activity: Activity) {
        startForResult(activity)
    }

    fun <T> startForResult(t: T) {
        try {
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).let { intent ->
                if (t is Fragment) {
                    t.startActivityForResult(intent, MainFragment.REQUEST_APPLICATION_DEVELOPMENT_SETTINGS)
                } else if (t is Activity) {
                    t.startActivityForResult(intent, MainFragment.REQUEST_APPLICATION_DEVELOPMENT_SETTINGS)
                } else {
                    throw IllegalArgumentException("Unauthorized parameter")
                }
            }
        } catch (e: ActivityNotFoundException) {
            Intent().let { intent ->
                intent.setComponent(
                    ComponentName(
                        "com.android.settings",
                        "com.android.settings.DevelopmentSettings")
                )
                intent.setAction("android.intent.action.View")
                if (t is Fragment) {
                    t.startActivityForResult(intent, MainFragment.REQUEST_APPLICATION_DEVELOPMENT_SETTINGS)
                } else if (t is Activity) {
                    t.startActivityForResult(intent, MainFragment.REQUEST_APPLICATION_DEVELOPMENT_SETTINGS)
                }
            }
        }
    }

    fun startOutsideOfActivity(context: Context) {
        try {
            Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).let { intent ->
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            }
        } catch (e: ActivityNotFoundException) {
            Intent().let { intent ->
                intent.setComponent(
                    ComponentName(
                        "com.android.settings",
                        "com.android.settings.DevelopmentSettings")
                )
                intent.setAction("android.intent.action.View")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            }
        }
    }
}