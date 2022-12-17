package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.annotation.RequiresApi
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.MainFragment.Companion.ACTION_SWITCH_OVERLAY_STATUS
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.MainFragment.Companion.KEY_OVERLAY_STATUS
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils.ServiceStatusChecker
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils.SettingsStarter


class OverlayService() : Service() {

    companion object {
        const val TAG = "OverlayService"
        const val CHANNEL_ID = "OverlayserviceChannel"
        const val NOTIFICATION_ID = 2
        const val ACTION_SWITCH_USB_DEBUG_STATUS = "ACTION_SWITCH_USB_DEBUG_STATUS"
    }


    // properties

    private val mHandler = Handler()
    private var mRunnable: Runnable? = null
    private var mOverlay: OverlayView? =  null

    private lateinit var mReceiver: BroadcastReceiver

    // methods

    override fun onCreate() {
        super.onCreate()
        setUpUsbDebugStatusReceiver()
    }

    private fun setUpUsbDebugStatusReceiver() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                mOverlay?.resetImage("")}
            }

        IntentFilter().let {
            it.addAction(ACTION_SWITCH_USB_DEBUG_STATUS)
            baseContext?.registerReceiver(mReceiver, it)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ServiceStatusChecker.isServiceRunningInForeground(
                getBaseContext(),
                "OverlayService"))
        {
            // 既に サービスがフォアグラウンドの場合は、startForground() をコールしない。
            // startForground() のコール自体は何回コールしても問題ないが、
            // notification への通知がその度に発生するのでUI的にうざいので、
            // 通知はフォアグラウンドに移行する際の一回でよい。
            return START_NOT_STICKY;
        }

        overlay()

        doStartForeground()

        return Service.START_NOT_STICKY
    }


    private fun overlay() {
        mOverlay?:run{
            mOverlay = OverlayView(this, object : OnSwitchUsbDebuggerListener {
                override fun onSwitch() {
//                    Intent(baseContext, MainActivity::class.java).let {
//                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        startActivity(it)
//                    }
                    SettingsStarter.startOutsideOfActivity(baseContext)
                }
            })
            mOverlay?.display( )
            setUpDebugStatusTimer()
        }
    }


    private fun setUpDebugStatusTimer() {
        // 定期実行 Runnable が有効の場合はキャンセルする
        mRunnable?.run {
            mHandler.removeCallbacks(mRunnable!!)
            mRunnable = null
        }

        // 定期実行 Runnable 生成する
        mRunnable = object : Runnable {
            override fun run() {
                // UIスレッド
                mOverlay?.resetImage("")
                mHandler.postDelayed(this, 3000)
            }
        }

        mHandler.post(mRunnable as Runnable)
    }


    private fun doStartForeground() {
          // 通知タップ時に発行する Intent を作成

        var notification: Notification? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationVersion26()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            createNotificationVersion25()
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            createNotificationVersion21()
        } else {
            null
        }

        startForeground(NOTIFICATION_ID, notification)

        Intent().let {
            it.action = ACTION_SWITCH_OVERLAY_STATUS
            it.putExtra(KEY_OVERLAY_STATUS,true)
            this.sendBroadcast(it)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationVersion26(): Notification {
        NotificationChannel(
                CHANNEL_ID,
                getString(R.string.noti_channel_overlay),
                NotificationManager.IMPORTANCE_DEFAULT
        ).let {
            it.lightColor = Color.GREEN
              // 通知時のライトの色
            it.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
              // ロック画面で通知を表示するかどうか
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(it)
        }

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_stat_adb)
              // 通知は 24 dp のアイコンをセットする。
              // アイコンカラーはモノクロになるので、ランチャーアイコンのような画像は向いていない。
            .setContentTitle(getString(R.string.notification_overlay_title))
//                      .setContentText("SubjectSubject")
              // ２行目の文字列設定。いらない。
//                      .setAutoCancel()
              // ユーザーがクリックで通知を削除できる。いらない。
            .setContentIntent(createPendingIntent())
            .build()
    }


    private fun createNotificationVersion25(): Notification {
        return Notification.Builder(this)
            .setSmallIcon(R.mipmap.ic_stat_adb)
            .setContentText(getString(R.string.notification_overlay_title))
            .setContentIntent(createPendingIntent())
            .build()
    }


    private fun createNotificationVersion21(): Notification {
        return Notification.Builder(this)
            .setSmallIcon(R.mipmap.ic_stat_adb)
            .setContentText(getString(R.string.notification_overlay_title))
            .setContentIntent(createPendingIntent())
            .getNotification()
    }


    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivities(this, 0, arrayOf(intent), PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onDestroy() {
        // オーバーレイを finalize
        mOverlay?.run {
            remove()
            mOverlay = null
        }

        // サービスを停止
        doStopForeground()

        // Activity にオーバーレイOFFを通知する
        Intent().let {
            it.action = ACTION_SWITCH_OVERLAY_STATUS
            it.putExtra(KEY_OVERLAY_STATUS,false)
            this.sendBroadcast(it)
        }

        // ブロードキャストレシーバーの finalize
        finalizeReceiver()

        // タイマーを finalize
        mRunnable?.run{
            mHandler.removeCallbacks(mRunnable!!)
            mRunnable = null
        }

        super.onDestroy()
    }

    private fun finalizeReceiver()  {
        baseContext?.unregisterReceiver(mReceiver)
    }


    private fun doStopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE or Service.STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(true)
        }
    }


    interface OnSwitchUsbDebuggerListener {
        fun onSwitch()
    }
}
