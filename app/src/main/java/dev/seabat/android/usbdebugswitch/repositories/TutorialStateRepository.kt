package dev.seabat.android.usbdebugswitch.repositories

import android.content.Context
import androidx.preference.PreferenceManager
import dev.seabat.android.usbdebugswitch.MainApplication
import dev.seabat.android.usbdebugswitch.constants.TutorialStateType

class TutorialStateRepository(
    private val context: Context = MainApplication.instance
) {

    /**
     * チュートリアルの表示済み状態を読み込む
     *
     * @return
     */
    fun load(): TutorialStateType {
        val preferenceData = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(
                "pref_tutorial",
                TutorialStateType.NOT_DISPLAYED.key
            ) ?: TutorialStateType.NOT_DISPLAYED.key
        return TutorialStateType.fromKey(preferenceData)
    }

    /**
     * チュートリアルの表示済み状態を保存する
     */
    fun save(state: TutorialStateType) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().let {editor ->
            editor.putString("pref_tutorial", state.key)
            editor.commit() // commit を忘れずに！
        }
    }
}