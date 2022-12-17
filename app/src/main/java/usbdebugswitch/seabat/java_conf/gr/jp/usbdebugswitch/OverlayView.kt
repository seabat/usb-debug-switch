package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.ImageView
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils.UsbDebugStatusChecker


class OverlayView(val mContext: Context, val mListener: OverlayService.OnSwitchUsbDebuggerListener) {


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

        val display = this.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        displaySize = size
        Log.d("XXX", "displaySize.x:" + displaySize.x + " displaySize.y:" + displaySize.y)

        // レイアウトファイルから重ね合わせするViewを作成する
        val layoutInflater = LayoutInflater.from(mContext)
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
                mListener.onSwitch()
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
                        val centerY = y - (displaySize.y / 2)

                        Log.d(TAG,
                            "tapX:" + x + " tapY:" + y + " fromCenterX:" + centerX + " fromCenterY:" + centerY)
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
