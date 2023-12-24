package dev.seabat.android.usbdebugswitch

import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import dev.seabat.android.usbdebugswitch.compose.MainScreen
import dev.seabat.android.usbdebugswitch.utils.OverlayPermissionChecker
import dev.seabat.android.usbdebugswitch.utils.SettingsLauncher
import dev.seabat.android.usbdebugswitch.utils.UsbDebugStatusChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class MainActivity : AppCompatActivity(), MessageDialogFragment.OnClickListener{

    companion object {
        const val TAG = "MainActivity"
        private val DEBUG = BuildConfig.DEBUG
        const val REQUEST_APPLICATION_DEVELOPMENT_SETTINGS = 20
        const val TAG_GOTO_OVERLAY_SETTING = "TAG_GOTO_OVERLAY_SETTING";
        const val ACTION_SWITCH_OVERLAY_STATUS = "ACTION_SWITCH_OVERLAY_STATUS"
        const val KEY_OVERLAY_STATUS = "KEY_OVERLAY_STATUS"
    }

    private lateinit var mReceiver: BroadcastReceiver

    enum class SetupStatusType(val order: Int) {
        READY(0),
        OVERLAY_VIEW(1),
        USB_DEBUG_VIEW(2), // オーバーレイ表示領域のセットアップ
        NOTIFICATION_PERMISSION(3),
        OVERLAY_RECEIVER(4),
        OVERLAY_PERMISSION(5),
        OVERLAY_SERVICE(6), // 通知パーミッションのセットアップ
        FINISH(7),
    }

    private var setupStatus = SetupStatusType.READY

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            proceedSetup(next = SetupStatusType.OVERLAY_RECEIVER)
        } else {
            Toast.makeText(
                this,
                "Please grant Post Notification Permission",
                Toast.LENGTH_SHORT
            )
        }
    }

    private val _overlayStateFlow = MutableStateFlow("")
    private val overlayStateFlow = _overlayStateFlow.asStateFlow()

    private val _usbDebugStateFlow = MutableStateFlow("")
    private val usbDebugStateFlow = _usbDebugStateFlow.asStateFlow()

    var appOpsManager: AppOpsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if(DEBUG) Log.d(TAG, "[${taskId}] onCreate")
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen(
                overlayStateFlow,
                usbDebugStateFlow,
                onOverlayCardClick = {
                    val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
                    val overlaySetting = sharedPref.getString("pref_setting_overlay", getString(R.string.setting_overlay_off))
                    if (overlaySetting == getString(R.string.setting_overlay_off)) {
                        tryToStartOverlayService()
                    } else {
                        stopOverlayService()
                    }
                },
                onUsbDebugCardClick = {
                    // 設定画面を起動する
                    SettingsLauncher.startForResultFromActivity(this@MainActivity)
                }
            )
        }

        // セットアップ開始
        proceedSetup(SetupStatusType.OVERLAY_VIEW)
    }

    override fun onStart() {
        if(DEBUG) Log.d(TAG, "[${taskId}] onStart")
        super.onStart()
    }


    override fun onResume() {
        if(DEBUG) Log.d(TAG, "[${taskId}] onResume")
        super.onResume()
    }


    override fun onStop() {
        if(DEBUG) Log.d(TAG, "[${taskId}] onStop")
        super.onStop()
    }


    override fun onDestroy() {
        if(DEBUG) Log.d(TAG, "[${taskId}] onDestroy")
        finalizeReceiver()
        super.onDestroy()
    }


    /**
     * MessageDialogFragment.OnClickListener の onClick
     */
    override fun onClick() {
        // 設定画面を起動する
        // 設定->アプリ->歯車アイコン->他のアプリの上に重ねて表示
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"))
        startActivity(intent)

        appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        appOpsManager?.startWatchingMode(
            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
            packageName,
            object : AppOpsManager.OnOpChangedListener {
                override fun onOpChanged(op: String?, packageName: String?) {
                    appOpsManager?.stopWatchingMode(this)    //監視を止める
                    if(OverlayPermissionChecker.isEnabled(this@MainActivity)) {
                        proceedSetup(next = SetupStatusType.OVERLAY_SERVICE)
                    } else {
                        proceedSetup(next = SetupStatusType.FINISH)
                    }
                }
            }
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_APPLICATION_DEVELOPMENT_SETTINGS -> {
                setUpUsbDebugView()
                Intent().let {
                    it.action = OverlayService.ACTION_SWITCH_USB_DEBUG_STATUS
                    sendBroadcast(it)
                }
            }
        }
    }

    private fun proceedSetup(next: SetupStatusType) {
        this.setupStatus = next
        Log.i("UsbDebugSwitch", "Setup status: $next")
        when(setupStatus) {
            SetupStatusType.READY -> {
                // Do nothing
            }
            SetupStatusType.OVERLAY_VIEW -> {
                setUpOverlayView()
            }
            SetupStatusType.USB_DEBUG_VIEW -> {
                setUpUsbDebugView()
            }
            SetupStatusType.NOTIFICATION_PERMISSION -> {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            SetupStatusType.OVERLAY_RECEIVER -> {
                setUpOverlayReceiver()
            }
            SetupStatusType.OVERLAY_PERMISSION -> {
                setUpOverlayPermission()
            }
            SetupStatusType.OVERLAY_SERVICE -> {
                startOverlayService()
            }
            SetupStatusType.FINISH -> {
                // Do nothing
            }
        }
    }

    /**
     * オーバーレイ preference を初期化する
     *
     * Preference に "ON" が格納されている場合は、オーバーレイサービスの開始を試みる。
     */
    private fun setUpOverlayPermission() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.getString(
            "pref_setting_overlay",
            getString(R.string.setting_overlay_off)
        ).let { statusString ->
            _overlayStateFlow.update { statusString!! }
            if (statusString == getString(R.string.setting_overlay_on)) {
                if (OverlayPermissionChecker.isEnabled(this)) {
                    proceedSetup(next = SetupStatusType.OVERLAY_SERVICE)
                } else {
                    // オーバーレイ権限がない場合、ダイアログを表示
                    MessageDialogFragment.newInstance(getString(R.string.dialog_msg_goto_overlay_setting))
                        .show(this.supportFragmentManager, MainActivity.TAG_GOTO_OVERLAY_SETTING)
                }
            } else {
                proceedSetup(next = SetupStatusType.FINISH)
            }
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)
        proceedSetup(next = SetupStatusType.FINISH)
    }

    /**
     * オーバーレイサービスを開始を試みる
     */
    private fun tryToStartOverlayService(){
        if (OverlayPermissionChecker.isEnabled(this)) {
            val intent = Intent(this, OverlayService::class.java)
            startService(intent)
        } else {
            // オーバーレイ権限がない場合、ダイアログを表示
            MessageDialogFragment.newInstance(getString(R.string.dialog_msg_goto_overlay_setting))
                .show(this.supportFragmentManager, MainActivity.TAG_GOTO_OVERLAY_SETTING)
        }
    }


    /**
     * オーバーレイ preference を無効にする
     */
    private fun disableOverlayPreference() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.edit().let {editor ->
            editor.putString("pref_setting_overlay", getString(R.string.setting_overlay_off))
            editor.commit() // commit を忘れずに！
        }

        _overlayStateFlow.update {
            getString(R.string.setting_overlay_off)
        }
    }

    /**
     * オーバーレイ preference を有効にする
     */
    private fun enableOverlayPreference() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.edit().let {editor ->
            editor.putString("pref_setting_overlay", getString(R.string.setting_usb_debug_on))
            editor.commit() // commit を忘れずに！
        }

        _overlayStateFlow.update {
            getString(R.string.setting_usb_debug_on)
        }
    }


    /**
     * オーバーレイサービスを開始する
     */
    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
    }

    private fun setUpOverlayView() {
        _overlayStateFlow.update { getString(R.string.setting_overlay_off) }
        proceedSetup(SetupStatusType.USB_DEBUG_VIEW)
    }

    private fun setUpUsbDebugView() {
        _usbDebugStateFlow.update {
            if (UsbDebugStatusChecker.isUsbDebugEnabled(this)) {
                getString(R.string.setting_usb_debug_on)
            } else {
                getString(R.string.setting_usb_debug_off)
            }
        }

        proceedSetup(SetupStatusType.NOTIFICATION_PERMISSION)
    }


    private fun setUpOverlayReceiver() {
        mReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if(intent?.getBooleanExtra(KEY_OVERLAY_STATUS,false) == true) {
                    enableOverlayPreference()
                } else {
                    disableOverlayPreference()
                }
            }
        }

        IntentFilter().apply {
            addAction(ACTION_SWITCH_OVERLAY_STATUS)
        }.let {
            registerReceiver(mReceiver, it)
        }

        proceedSetup(SetupStatusType.OVERLAY_PERMISSION)
    }

    private fun finalizeReceiver()  {
        unregisterReceiver(mReceiver)
    }
}
