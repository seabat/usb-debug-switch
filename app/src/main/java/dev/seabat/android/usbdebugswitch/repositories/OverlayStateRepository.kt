package dev.seabat.android.usbdebugswitch.repositories

import android.content.Context
import androidx.preference.PreferenceManager
import dev.seabat.android.usbdebugswitch.MainApplication
import dev.seabat.android.usbdebugswitch.constants.OverlayStateType

class OverlayStateRepository(
    private val context: Context = MainApplication.instance
) {


    /**
     * オーバーレイON/OFF状態を読み込む
     *
     * @return
     */
    fun load(): OverlayStateType {
        val preferenceData = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(
                "pref_overlay_state",
                OverlayStateType.OFF.key
            ) ?: OverlayStateType.OFF.key
        return OverlayStateType.fromKey(preferenceData)
    }

    fun save(onOff: OverlayStateType) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().let {editor ->
            editor.putString("pref_overlay_state", onOff.key)
            editor.commit() // commit を忘れずに！
        }
    }

    /**
     * オーバーレイが有効か
     */
    fun isEnabled(): Boolean {
        return load() == OverlayStateType.ON
    }
}