package dev.seabat.android.usbdebugswitch.utils

import android.util.Log
import dev.seabat.android.usbdebugswitch.BuildConfig

/**
 * デバッグビルドの場合のみ Logcat へ出力する。リリースビルドでは出力しない。
 */
object Logger {

    fun v(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.v(tag, message)
    }

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d(tag, message)
    }

    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.w(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) Log.e(tag, message, throwable)
    }
}
