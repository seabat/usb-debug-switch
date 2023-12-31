package dev.seabat.android.usbdebugswitch.utils

import android.app.Activity
import android.content.*
import android.provider.Settings
import androidx.fragment.app.Fragment
import dev.seabat.android.usbdebugswitch.MainActivity.Companion.REQUEST_APPLICATION_DEVELOPMENT_SETTINGS

/**
 * 設定アプリの「開発者向けオプション」画面を起動するクラス
 */
object DeveloperOptionsLauncher {
    fun startForResultFromFragment(frg: Fragment) {
        startForResult(frg)
    }


    /**
     * Activity から設定アプリの「開発者向けオプション」画面を起動する
     */
    fun startForResultFromActivity(activity: Activity) {
        startForResult(activity)
    }

    /**
     * 設定アプリの「開発者向けオプション」画面を起動する
     */
    private fun <T> startForResult(t: T) {
        try {
            Intent().apply {
                action = "com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS"
                // NOTE: action = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS だと
                //       startActivityForResult で 「開発者向けオプション」画面は起動するが onActivityResult
                //       が起動しないので注意する。
            }.let {
                if (t is Fragment) {
                    t.startActivityForResult(it, REQUEST_APPLICATION_DEVELOPMENT_SETTINGS)
                } else if (t is Activity) {
                    t.startActivityForResult(it, REQUEST_APPLICATION_DEVELOPMENT_SETTINGS)
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
                    t.startActivityForResult(intent, REQUEST_APPLICATION_DEVELOPMENT_SETTINGS)
                } else if (t is Activity) {
                    t.startActivityForResult(intent, REQUEST_APPLICATION_DEVELOPMENT_SETTINGS)
                }
            }
        }
    }

    /**
     * Activity ではないところから設定アプリの「開発者向けオプション」画面を起動する
     */
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