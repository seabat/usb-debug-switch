package dev.seabat.android.usbdebugswitch.view

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
import android.widget.ImageView
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.constants.SelectedOverlayType
import dev.seabat.android.usbdebugswitch.repositories.InternetStateRepository
import dev.seabat.android.usbdebugswitch.repositories.SelectedOverlayRepository
import dev.seabat.android.usbdebugswitch.services.OverlayService
import dev.seabat.android.usbdebugswitch.utils.UsbDebugStatusChecker


class OverlayView(val mContext: Context, val mListener: OverlayService.OnSwitchListener) {


    private val mOverlayView: ViewGroup

    private val windowManager: WindowManager

    private val displaySize: Point

    private val mParams: WindowManager.LayoutParams

    private var mIsLongClick = false

    companion object {
        val TAG = "OverlayView"
    }

    init {
        this.windowManager = mContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // NOTE: API 30 からは Service から受け取った Context を使用して display 操作をしてはいけないらしく、
        //       以下のエラーが発生する。
        //       ```
        //       java.lang.UnsupportedOperationException: Tried to obtain display from a Context
        //       not associated with one. Only visual Contexts (such as Activity or one created
        //       with Context#createWindowContext) or ones created with Context#createDisplayContext
        //       are associated with displays. Other types of Contexts are typically related to
        //       background entities and may return an arbitrary display.
        //       ```
        //       Context#createDisplayContext の使い方は https://pisuke-code.com/android-getter-for-defaultdisplay-display-is-deprecated/
        //       を参考にする。

        val layoutInflater = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            val display = this.windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            displaySize = size
            Log.d("XXX", "displaySize.x:" + displaySize.x + " displaySize.y:" + displaySize.y)
            LayoutInflater.from(mContext)
        } else {
            val displayManager = mContext.getSystemService<DisplayManager>()
                ?.getDisplay(Display.DEFAULT_DISPLAY)
            val displayContext = mContext.createDisplayContext(displayManager!!)
            val screenWidth = displayContext.resources.displayMetrics.widthPixels
            val screenHeight = displayContext.resources.displayMetrics.heightPixels
            displaySize = Point(screenWidth, screenHeight)
            Log.d("XXX", "displaySize.x:" + displaySize.x + " displaySize.y:" + displaySize.y)
            LayoutInflater.from(displayContext)
        }

        // レイアウトファイルから重ね合わせするViewを作成する
        mOverlayView = layoutInflater.inflate(R.layout.overlay, null) as ViewGroup
        registerImage()
        setupOverlayListener()

        // 重ね合わせするViewの設定を行う
        mParams = createLayoutParams()
    }

    // methods

    fun display() {
        // Viewを画面上に重ね合わせて表示する
        this.windowManager.let { windowManager ->
            windowManager.addView(mOverlayView, mParams)
        }
        Log.d(TAG, "mParams.x:" + mParams.x + " mParams.y:" + mParams.y)
    }

    /**
     * オーバーレイのレイアウトをドラッグ&ドロップできるようにリスナーを設定する
     *
     * ref. https://qiita.com/farman0629/items/ce547821dd2e16e4399e
     */
    private fun setupOverlayListener() {
        val imageView = mOverlayView.findViewById(R.id.debug_onoff_image) as ImageView

        mOverlayView.setOnClickListener {
            if (!mIsLongClick) {
                imageView.visibility = View.INVISIBLE
                Handler().postDelayed({ imageView.visibility = View.VISIBLE }, 500L)
                when(SelectedOverlayRepository().load()) {
                    SelectedOverlayType.USB_DEBUG -> {
                        mListener.onUsbDebugSwitch()
                    }
                    SelectedOverlayType.INTERNET -> {
                        mListener.onInternetSwitch()
                    }
                }
            }
        }

        mOverlayView.setOnLongClickListener {
            mIsLongClick = true
            imageView.setAlpha(130) // 透明度を設定する MAX:255
            false
        }

        mOverlayView.setOnTouchListener { view, motionEvent ->
            //タップした位置
            val x = motionEvent.rawX.toInt()
            val y = motionEvent.rawY.toInt()

            when (motionEvent.action) {
                // Viewを移動させてるときに呼ばれる
                MotionEvent.ACTION_MOVE -> {
                    if (mIsLongClick) {
                        // 中心からの座標を計算する
                        val centerX = x - (displaySize.x / 2)
                        var centerY = y - (displaySize.y / 2)

                        Log.d(
                            TAG,
                            "tapX:" + x + " tapY:" + y + " fromCenterX:" + centerX + " fromCenterY:" + centerY)

                        // 微調整
                        centerY -= 80

                        // オーバーレイ表示領域の座標を中心からの座標位置に移動させる
                        mParams.x = centerX
                        mParams.y = centerY

                        // NOTE: View の gravity を設定すると上記の座標計算が利用できないので
                        //       View には gravity を設定しないこと

                        // 移動した分を更新する
                        windowManager.updateViewLayout(mOverlayView, mParams)
                    }
                }
                // Viewの移動が終わったときに呼ばれる
                MotionEvent.ACTION_UP -> {
                    if (mIsLongClick) {
                        Handler().postDelayed({ mIsLongClick = false }, 500L)
                        imageView.setAlpha(255)
                    }
                }
            }
            false
        }
    }


    /**
     * ImageView に USBデバッグ設定の状態を表現する画像をセットする
     */
    private fun registerImage() {
        val imageView = mOverlayView.findViewById(R.id.debug_onoff_image) as ImageView
        when(SelectedOverlayRepository().load()) {
            SelectedOverlayType.USB_DEBUG -> {
                if (UsbDebugStatusChecker.isUsbDebugEnabled(mContext)) {
                    imageView.setImageResource(R.mipmap.ic_on)
                } else {
                    imageView.setImageResource(R.mipmap.ic_off)
                }
            }
            SelectedOverlayType.INTERNET -> {
                if (InternetStateRepository().isEnabled()) {
                    imageView.setImageResource(R.mipmap.ic_online)
                } else {
                    imageView.setImageResource(R.mipmap.ic_offline)
                }
            }
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

        return params
    }


    /**
     * オーバーレイで表示する画像をリセットする
     */
    fun resetImage() {
        registerImage()
    }


    fun remove() {
        // サービスが破棄されるときには重ね合わせしていたViewを削除する
        this.windowManager.let { windowManager ->
            windowManager.removeView(mOverlayView)
        }
    }
}
