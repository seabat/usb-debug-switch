package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils.UsbDebugStatusChecker


class OverlayView(var mContext: Context) {


    private var mView: View? = null

    private var mListener: OverlayService.OnSwitchUsbDebuggerListener? = null


    companion object {
        val TAG = "OverlayView"
        private var mOverlayView: OverlayView? = null
    }

    // methods

    fun display(listener: OverlayService.OnSwitchUsbDebuggerListener) {
        if (mView != null) {
            return
        }

        mListener = listener

        val layoutInflater = LayoutInflater.from(mContext)
          // Viewからインフレータを作成する
        mView = layoutInflater.inflate(R.layout.overlay, null)
          // レイアウトファイルから重ね合わせするViewを作成する

        mView?.let {
            setupImage(it.findViewById(R.id.debug_onoff_image) as ImageView)
        }

        val params = createLayoutParams()
          // 重ね合わせするViewの設定を行う

        (mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).let { windowManager ->
            windowManager.addView(mView, params as ViewGroup.LayoutParams?)
              // Viewを画面上に重ね合わせする
        }
    }

    private fun setupImage(imageView: ImageView) {
        registerImage(imageView)

        imageView.setOnClickListener {
            imageView.visibility = View.INVISIBLE
            Handler().postDelayed({ imageView.visibility = View.VISIBLE }, 500L)

            mListener?.onSwitch()
        }

        imageView.setOnLongClickListener {
            //TODO: ロングクリック時にサービスをストップする
            false
        }
    }


    /**
     * ImageView に USBデバッグ設定の状態を表現する画像をセットする
     */
    private fun registerImage(imageView: ImageView) {
        if (UsbDebugStatusChecker.isUsbDebugEnabled(mContext)) {
            imageView.setImageResource(R.mipmap.ic_on)
        } else {
            imageView.setImageResource(R.mipmap.ic_off)
        }
    }


    private fun createLayoutParams(): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT

        )

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext)
        params.gravity = OverlayPositionPreferenceConverter.convertPrefValueToViewValue(
            sharedPref.getString(
                "pref_vertical_axis",
                mContext.resources.getString(R.string.pref_vertical_axis_default)
            )
        ) or OverlayPositionPreferenceConverter.convertPrefValueToViewValue(
            sharedPref.getString(
                "pref_horizontal_axis",
                mContext.resources.getString(R.string.pref_horizontal_axis_default)
            )
        )
        // ex. Gravity.CENTER | Gravity.TOP;

        return params
    }


    /**
     * オーバーレイで表示する画像をリセットする
     */
    fun resetImage(imageString: String) {
        mView?.let {
            registerImage(it.findViewById(R.id.debug_onoff_image) as ImageView)
        }
    }


    fun remove() {
        // サービスが破棄されるときには重ね合わせしていたViewを削除する
        (mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager).let { windowManager ->
            windowManager.removeView(mView)
        }
        mView = null
    }
}
