package dev.seabat.android.usbdebugswitch.services

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
import dev.seabat.android.usbdebugswitch.MainActivity
import dev.seabat.android.usbdebugswitch.view.OverlayView
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.constants.OverlayStateType
import dev.seabat.android.usbdebugswitch.constants.SelectedOverlayType
import dev.seabat.android.usbdebugswitch.pages.HomeFragment.Companion.ACTION_LAUNCH_DEVELOPER_OPTIONS
import dev.seabat.android.usbdebugswitch.pages.HomeFragment.Companion.ACTION_SWITCH_INTERNET
import dev.seabat.android.usbdebugswitch.pages.HomeFragment.Companion.ACTION_SWITCH_OVERLAY_STATUS
import dev.seabat.android.usbdebugswitch.pages.HomeFragment.Companion.KEY_INTERNET_STATUS
import dev.seabat.android.usbdebugswitch.pages.HomeFragment.Companion.KEY_OVERLAY_STATUS
import dev.seabat.android.usbdebugswitch.pages.HomeFragment.Companion.KEY_SELECTED_OVERLAY
import dev.seabat.android.usbdebugswitch.repositories.InternetStateRepository
import dev.seabat.android.usbdebugswitch.repositories.OverlayStateRepository
import dev.seabat.android.usbdebugswitch.repositories.SelectedOverlayRepository
import dev.seabat.android.usbdebugswitch.utils.ServiceStatusChecker
import dev.seabat.android.usbdebugswitch.utils.DeveloperOptionsLauncher


class OverlayService() : Service() {

    companion object {
        const val TAG = "OverlayService"
        const val CHANNEL_ID = "OverlayserviceChannel"
        const val NOTIFICATION_ID = 2
        const val ACTION_SWITCH_INTERNET_STATUS = "ACTION_SWITCH_INTERNET_STATUS"
        const val ACTION_SWITCH_USB_DEBUG_STATUS = "ACTION_SWITCH_USB_DEBUG_STATUS"
        const val ACTION_SELECT_OVERLAY_SETTING = "ACTION_SELECT_OVERLAY_SETTING"
        const val INTENT_ITEM_SELECTED_OVERLAY = "INTENT_ITEM_SELECTED_OVERLAY"
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

    @RequiresApi(Build.VERSION_CODES.O) // for RECEIVER_NOT_EXPORTED
    private fun setUpUsbDebugStatusReceiver() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_SWITCH_INTERNET_STATUS,
                    ACTION_SWITCH_USB_DEBUG_STATUS -> {
                        mOverlay?.resetImage()
                    }
                    ACTION_SELECT_OVERLAY_SETTING -> {
                        SelectedOverlayRepository().save(
                            intent.getStringExtra(INTENT_ITEM_SELECTED_OVERLAY).let {
                                if (it == null) {
                                    SelectedOverlayType.USB_DEBUG
                                } else {
                                    SelectedOverlayType.fromKey(it)
                                }
                            }
                        )
                        mOverlay?.resetImage()
                        sendOverlayStatusToActivity()
                    }
                    else -> {}
                }
            }
        }
        baseContext?.registerReceiver(mReceiver,
            IntentFilter().apply {
                addAction(ACTION_SWITCH_INTERNET_STATUS)
                addAction(ACTION_SWITCH_USB_DEBUG_STATUS)
                addAction(ACTION_SELECT_OVERLAY_SETTING)
            }, RECEIVER_NOT_EXPORTED
        )
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ServiceStatusChecker.isServiceRunningInForeground(
                getBaseContext(),
                "OverlayService"))
        {
            // 既に サービスがフォアグラウンドの場合は、startForeground() をコールしない。
            // startForeground() のコール自体は何回コールしても問題ないが、
            // notification への通知がその度に発生するのでUI的にうざいので、
            // 通知はフォアグラウンドに移行する際の一回でよい。
            return START_NOT_STICKY;
        }

        doStartForeground {
            overlay()
            OverlayStateRepository().save(OverlayStateType.ON)
            sendOverlayStatusToActivity()
        }

        return START_NOT_STICKY
    }


    private fun overlay() {
        mOverlay ?: run {
            mOverlay = OverlayView(this, object : OnSwitchListener {
                override fun onUsbDebugSwitch() {
                    sendBroadcast(
                        Intent().apply {
                            action = ACTION_LAUNCH_DEVELOPER_OPTIONS
                        }
                    )
                }

                override fun onInternetSwitch() {
                    sendSwitchInternetToActivity()
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
                mOverlay?.resetImage()
                mHandler.postDelayed(this, 5000)
            }
        }

        mHandler.post(mRunnable as Runnable)
    }


    private fun doStartForeground(onStartService: () -> Unit) {
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

        onStartService()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationVersion26(): Notification {
        NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_overlay),
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
        return PendingIntent.getActivities(this,
            0,
            arrayOf(intent),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }


    override fun onDestroy() {
        // オーバーレイを finalize
        mOverlay?.run {
            remove()
            mOverlay = null
        }

        // サービスを停止
        doStopForeground {
            OverlayStateRepository().save(OverlayStateType.OFF)
            sendOverlayStatusToActivity()
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

    private fun sendOverlayStatusToActivity() {
        sendBroadcast(
            Intent().apply {
                action = ACTION_SWITCH_OVERLAY_STATUS
                putExtra(KEY_OVERLAY_STATUS,OverlayStateRepository().load().key)
                putExtra(KEY_SELECTED_OVERLAY, SelectedOverlayRepository().load().key)
            }
        )
    }

    private fun sendSwitchInternetToActivity() {
        sendBroadcast(
            Intent().apply {
                action = ACTION_SWITCH_INTERNET
                putExtra(KEY_INTERNET_STATUS, !InternetStateRepository().isEnabled())
            }
        )
    }

    private fun doStopForeground(onStopService: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE or Service.STOP_FOREGROUND_DETACH)
        } else {
            stopForeground(true)
        }
        onStopService()
    }


    interface OnSwitchListener {
        /**
         * オーバーレイ表示されているUSB デバッグアイコンがクリックされた
         */
        fun onUsbDebugSwitch()
        fun onInternetSwitch()
    }
}
