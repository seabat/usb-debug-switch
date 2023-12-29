package dev.seabat.android.usbdebugswitch.repositories

import android.content.Context
import androidx.preference.PreferenceManager
import dev.seabat.android.usbdebugswitch.MainApplication
import dev.seabat.android.usbdebugswitch.R
import kotlinx.coroutines.flow.update

class OverlaySettingRepository(
    private val context: Context = MainApplication.instance
) {


    /**
     * オーバーレイ設定を読み込む
     *
     * @return "ON", "OFF"
     */
    fun load(): String {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(
                "pref_setting_overlay",
                context.getString(R.string.setting_overlay_off
                )
            ) ?: context.getString(R.string.setting_overlay_off)
    }

    fun save(onOff: String) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().let {editor ->
            editor.putString("pref_setting_overlay", onOff)
            editor.commit() // commit を忘れずに！
        }
    }

    /**
     * オーバーレイが有効か
     */
    fun isEnabled(): Boolean {
        return load() == context.getString(R.string.setting_overlay_on)
    }
}