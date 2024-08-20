package dev.seabat.android.usbdebugswitch.utils

import android.content.*
import android.provider.Settings

/**
 * 設定アプリの「開発者向けオプション」画面を起動するクラス
 */
object DeveloperOptionsLauncher {
    fun startActivityForResult(launchActivityResultLauncher: (Intent) -> Unit) {
        Intent().apply {
            action = "com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS"
            // NOTE: action = Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS だと
            //       startActivityForResult で 「開発者向けオプション」画面は起動するが onActivityResult
            //       が起動しないので注意する。
        }.let {
            launchActivityResultLauncher(it)
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
                        "com.android.settings.DevelopmentSettings"
                    )
                )
                intent.setAction("android.intent.action.View")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(intent)
            }
        }
    }
}