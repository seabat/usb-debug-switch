package dev.seabat.android.usbdebugswitch.repositories

import android.content.Context
import androidx.preference.PreferenceManager
import dev.seabat.android.usbdebugswitch.MainApplication
import dev.seabat.android.usbdebugswitch.constants.SelectedOverlayType

class SelectedOverlayRepository(private val context: Context = MainApplication.instance) {

    /**
     * 選択したオーバーレイ種別を読み込む
     *
     * @return
     */
    fun load(): SelectedOverlayType {
        val preferenceData = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(
                "pref_overlay_setting",
                SelectedOverlayType.USB_DEBUG.key
            ) ?: SelectedOverlayType.USB_DEBUG.key
        return SelectedOverlayType.fromKey(preferenceData)
    }

    /**
     * 選択したオーバーレイ種別を保存する
     */
    fun save(overlayType: SelectedOverlayType) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().let { editor ->
            editor.putString("pref_overlay_setting", overlayType.key)
            editor.commit() // commit を忘れずに！
        }
    }
}