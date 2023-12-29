package dev.seabat.android.usbdebugswitch

import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import dev.seabat.android.usbdebugswitch.compose.MainScreen
import dev.seabat.android.usbdebugswitch.dialog.PermissionWarningDialog
import dev.seabat.android.usbdebugswitch.repositories.InternetSettingRepository
import dev.seabat.android.usbdebugswitch.repositories.OverlaySettingRepository
import dev.seabat.android.usbdebugswitch.utils.CheckNotificationPermission
import dev.seabat.android.usbdebugswitch.utils.CheckOverlayPermission
import dev.seabat.android.usbdebugswitch.utils.DeveloperOptionsLauncher
import dev.seabat.android.usbdebugswitch.utils.UsbDebugStatusChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainActivity : AppCompatActivity(){

    companion object {
        const val TAG = "MainActivity"
        private val DEBUG = BuildConfig.DEBUG
        const val REQUEST_APPLICATION_DEVELOPMENT_SETTINGS = 20
        const val TAG_GOTO_OVERLAY_SETTING = "TAG_GOTO_OVERLAY_SETTING"
        const val TAG_GOTO_APP_SETTING = "TAG_GOTO_APP_SETTING"
        const val ACTION_SWITCH_OVERLAY_STATUS = "ACTION_SWITCH_OVERLAY_STATUS"
        const val KEY_OVERLAY_STATUS = "KEY_OVERLAY_STATUS"
    }

    private lateinit var mReceiver: BroadcastReceiver
    private lateinit var mWifiStateReceiver: BroadcastReceiver

    enum class SetupStatusType(val order: Int) {
        READY(0),
        OVERLAY_VIEW(1),
        USB_DEBUG_VIEW(2), // オーバーレイ表示領域のセットアップ
        NOTIFICATION_PERMISSION(3),
        OVERLAY_RECEIVER(4),
        OVERLAY_PERMISSION(5),
        OVERLAY_SERVICE(6), // 通知パーミッションのセットアップ
        WIFI_STATE_RECEIVER(7),
        FINISH(8),
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

    private val _internetStateFlow = MutableStateFlow("")
    private val internetStateFlow = _internetStateFlow.asStateFlow()

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
                internetStateFlow = internetStateFlow,
                overlayStateFlow = overlayStateFlow,
                usbDebugStateFlow = usbDebugStateFlow,
                onOverlaySwitch = {
                    if (!OverlaySettingRepository().isEnabled()) {
                        tryToStartOverlayService()
                    } else {
                        stopOverlayService()
                    }
                },
                onUsbDebugSwitch = {
                    // 設定画面を起動する
                    DeveloperOptionsLauncher.startForResultFromActivity(this@MainActivity)
                },
                onInternetSwitch = {
                    InternetSettingRepository().setEnabled(false, this)
                }
            )
        }

        supportFragmentManager.setFragmentResultListener(
            TAG_GOTO_OVERLAY_SETTING,
            this
        ) { _, _ ->
            launchOverlaySetting()
        }

        supportFragmentManager.setFragmentResultListener(
            TAG_GOTO_APP_SETTING,
            this
        ) { _, _ ->
            launchAppSetting()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            // 「開発者向けオプション」画面から戻った本アプリに戻った場合
            REQUEST_APPLICATION_DEVELOPMENT_SETTINGS -> {
                setupUsbDebugView()
                Intent().let {
                    it.action = OverlayService.ACTION_SWITCH_USB_DEBUG_STATUS
                    sendBroadcast(it)
                }
            }
        }
    }

    /**
     * オーバーレイの設定画面を起動する
     */
    private fun launchOverlaySetting() {
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
                    CheckOverlayPermission(this@MainActivity)(
                        enabled = { proceedSetup(next = SetupStatusType.OVERLAY_SERVICE) },
                        disabled = { proceedSetup(next = SetupStatusType.WIFI_STATE_RECEIVER) }
                    )
                }
            }
        )
    }

    private fun launchAppSetting() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.parse("package:dev.seabat.android.usbdebugswitch")
        }
        startActivity(intent)
    }

    private fun proceedSetup(next: SetupStatusType) {
        this.setupStatus = next
        Log.i("UsbDebugSwitch", "Setup status: $next")
        when(setupStatus) {
            SetupStatusType.READY -> {
                // Do nothing
            }
            SetupStatusType.OVERLAY_VIEW -> {
                setupOverlayView()
            }
            SetupStatusType.USB_DEBUG_VIEW -> {
                setupUsbDebugView()
            }
            SetupStatusType.NOTIFICATION_PERMISSION -> {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            SetupStatusType.OVERLAY_RECEIVER -> {
                setupOverlayReceiver()
            }
            SetupStatusType.OVERLAY_PERMISSION -> {
                setupOverlayPermission()
            }
            SetupStatusType.OVERLAY_SERVICE -> {
                startOverlayService()
            }
            SetupStatusType.WIFI_STATE_RECEIVER -> {
                setupWifiStateReceiver()
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
    private fun setupOverlayPermission() {
        _overlayStateFlow.update{ OverlaySettingRepository().load() }
        if (OverlaySettingRepository().isEnabled()) {
            CheckOverlayPermission(this)(
                enabled = {
                    proceedSetup(next = SetupStatusType.OVERLAY_SERVICE)
                },
                disabled = {
                    // オーバーレイ権限がない場合、ダイアログを表示
                    PermissionWarningDialog
                        .newInstance(getString(R.string.dialog_msg_goto_overlay_setting), TAG_GOTO_OVERLAY_SETTING)
                        .show(this.supportFragmentManager, MainActivity.TAG_GOTO_OVERLAY_SETTING)
                }
            )
        } else {
            proceedSetup(next = SetupStatusType.WIFI_STATE_RECEIVER)
        }
    }

    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        startService(intent)
        proceedSetup(next = SetupStatusType.WIFI_STATE_RECEIVER)
    }

    /**
     * オーバーレイサービスを開始を試みる
     */
    private fun tryToStartOverlayService(){
        // 通知パーミッションをチェック
        CheckNotificationPermission(this)(
            enabled = {
                // オーバーレイ権限をチェック
                CheckOverlayPermission(this)(
                    enabled ={
                        // オーバーレイサービスを開始
                        val intent = Intent(this, OverlayService::class.java)
                        startService(intent)
                    },
                    disabled = {
                        // オーバーレイ権限がない場合、ダイアログを表示
                        PermissionWarningDialog
                            .newInstance(getString(R.string.dialog_msg_goto_overlay_setting), TAG_GOTO_OVERLAY_SETTING)
                            .show(this.supportFragmentManager, MainActivity.TAG_GOTO_OVERLAY_SETTING)
                    }
                )
            },
            disabled = {
                // パーミッション警告ダイアログを表示
                PermissionWarningDialog
                    .newInstance(getString(R.string.notification_permission_dialog_message), TAG_GOTO_APP_SETTING)
                    .show(this.supportFragmentManager, TAG_GOTO_APP_SETTING)
            }
        )
    }

    /**
     * オーバーレイ preference を無効にする
     */
    private fun disableOverlayPreference() {
        OverlaySettingRepository().save(getString(R.string.setting_overlay_off))
        _overlayStateFlow.update {
            getString(R.string.setting_overlay_off)
        }
    }

    /**
     * オーバーレイ preference を有効にする
     */
    private fun enableOverlayPreference() {
        OverlaySettingRepository().save(getString(R.string.setting_usb_debug_on))
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

    private fun setupOverlayView() {
        _overlayStateFlow.update { getString(R.string.setting_overlay_off) }
        proceedSetup(SetupStatusType.USB_DEBUG_VIEW)
    }

    private fun setupUsbDebugView() {
        _usbDebugStateFlow.update {
            if (UsbDebugStatusChecker.isUsbDebugEnabled(this)) {
                getString(R.string.setting_usb_debug_on)
            } else {
                getString(R.string.setting_usb_debug_off)
            }
        }

        proceedSetup(SetupStatusType.NOTIFICATION_PERMISSION)
    }


    private fun setupOverlayReceiver() {
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

    private fun setupWifiStateReceiver() {
        mWifiStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                    when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
                        WifiManager.WIFI_STATE_ENABLED -> {
                            _internetStateFlow.update { getString(R.string.setting_internet_on) }
                        }
                        WifiManager.WIFI_STATE_DISABLED -> {
                            _internetStateFlow.update { getString(R.string.setting_internet_off) }
                        }
                    }
                }
            }
        }

        IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }.let {
            registerReceiver(mWifiStateReceiver, it)
        }

        proceedSetup(SetupStatusType.FINISH)
    }

    private fun finalizeReceiver()  {
        unregisterReceiver(mReceiver)
        unregisterReceiver(mWifiStateReceiver)
    }
}
