package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
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


    private val mView: View

    private var mListener: OverlayService.OnSwitchUsbDebuggerListener? = null

    private val windowManager: WindowManager

    private val displaySize: Point

    companion object {
        val TAG = "OverlayView"
        private var mOverlayView: OverlayView? = null
    }

    init {
        this.windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val display = this.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        displaySize = size

        // レイアウトファイルから重ね合わせするViewを作成する
        val layoutInflater = LayoutInflater.from(mContext)
        mView = layoutInflater.inflate(R.layout.overlay, null).also {
            setupImage(it.findViewById(R.id.debug_onoff_image) as ImageView)
        }
    }

    // methods

    fun display(listener: OverlayService.OnSwitchUsbDebuggerListener) {
        mListener = listener


        val params = createLayoutParams()
          // 重ね合わせするViewの設定を行う

        this.windowManager.let { windowManager ->
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
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext)
        params.gravity = OverlayPositionPreferenceConverter.convertPrefValueToViewValue(
            sharedPref.getString(
                "pref_vertical_axis",
                mContext.resources.getString(R.string.pref_vertical_axis_default)
            )!!
        ) or OverlayPositionPreferenceConverter.convertPrefValueToViewValue(
            sharedPref.getString(
                "pref_horizontal_axis",
                mContext.resources.getString(R.string.pref_horizontal_axis_default)
            )!!
        )
        // ex. Gravity.CENTER | Gravity.TOP;

        return params
    }


    /**
     * オーバーレイで表示する画像をリセットする
     */
    fun resetImage(imageString: String) {
        registerImage(this.mView.findViewById(R.id.debug_onoff_image) as ImageView)
    }


    fun remove() {
        // サービスが破棄されるときには重ね合わせしていたViewを削除する
        this.windowManager.let { windowManager ->
            windowManager.removeView(mView)
        }
    }
}
