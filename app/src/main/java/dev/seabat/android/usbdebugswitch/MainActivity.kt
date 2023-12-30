package dev.seabat.android.usbdebugswitch

import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import dev.seabat.android.usbdebugswitch.compose.MainScreen
import dev.seabat.android.usbdebugswitch.constants.InternetStateType
import dev.seabat.android.usbdebugswitch.constants.OverlayStateType
import dev.seabat.android.usbdebugswitch.constants.SelectedOverlayType
import dev.seabat.android.usbdebugswitch.constants.UsbDebugStateType
import dev.seabat.android.usbdebugswitch.dialog.PermissionWarningDialog
import dev.seabat.android.usbdebugswitch.repositories.InternetStateRepository
import dev.seabat.android.usbdebugswitch.repositories.OverlayStateRepository
import dev.seabat.android.usbdebugswitch.repositories.SelectedOverlayRepository
import dev.seabat.android.usbdebugswitch.services.OverlayService
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
        const val ACTION_SWITCH_INTERNET = "ACTION_SWITCH_INTERNET"
        const val KEY_OVERLAY_STATUS = "KEY_OVERLAY_STATUS"
        const val KEY_SELECTED_OVERLAY = "KEY_SELECTED_OVERLAY"
        const val KEY_INTERNET_STATUS = "KEY_INTERNET_STATUS"
    }

    private lateinit var mOverlayStatusReceiver: BroadcastReceiver
    private lateinit var mWifiStateReceiver: BroadcastReceiver

    enum class SetupStatusType(val order: Int) {
        READY(0),
        INTERNET_VIEW(1),
        OVERLAY_VIEW(2),
        USB_DEBUG_VIEW(3), // オーバーレイ表示領域のセットアップ
        NOTIFICATION_PERMISSION(4),
        OVERLAY_RECEIVER(5),
        OVERLAY_PERMISSION(6),
        OVERLAY_SERVICE(7), // 通知パーミッションのセットアップ
        WIFI_STATE_RECEIVER(8),
        FINISH(9),
    }

    private var setupStatus = SetupStatusType.READY

    private val requestNotificationPermissionLauncher = registerForActivityResult(
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

    private val _internetStateFlow = MutableStateFlow(InternetStateType.OFF)
    private val internetStateFlow = _internetStateFlow.asStateFlow()

    private val _overlayStateFlow = MutableStateFlow(OverlayStateType.OFF)
    private val overlayStateFlow = _overlayStateFlow.asStateFlow()

    private val _usbDebugStateFlow = MutableStateFlow(UsbDebugStateType.OFF)
    private val usbDebugStateFlow = _usbDebugStateFlow.asStateFlow()

    private val _selectSettingStateFlow = MutableStateFlow(SelectedOverlayType.USB_DEBUG)
    private val selectSettingStateFlow = _selectSettingStateFlow.asStateFlow()

    var appOpsManager: AppOpsManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if(DEBUG) Log.d(TAG, "[${taskId}] onCreate")
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen(
                internetStateFlow = internetStateFlow,
                overlayStateFlow = overlayStateFlow,
                usbDebugStateFlow = usbDebugStateFlow,
                selectedSettingStateFlow = selectSettingStateFlow,
                onOverlaySwitch = {
                    if (!OverlayStateRepository().isEnabled()) {
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
                    InternetStateRepository().setEnabled(it == InternetStateType.ON, this)
                },
                onToggleSetting = {
                    sendCommandToOverlayService(it)
                }
            )
        }

        supportFragmentManager.setFragmentResultListener(
            TAG_GOTO_OVERLAY_SETTING,
            this
        ) { _, _ ->
            launchOverlayStateSetting()
        }

        supportFragmentManager.setFragmentResultListener(
            TAG_GOTO_APP_SETTING,
            this
        ) { _, _ ->
            launchAppSetting()
        }

        // セットアップ開始
        proceedSetup(SetupStatusType.INTERNET_VIEW)
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
                // オーバーレイサービスに「開発者向けオプション」画面から戻ったことを通知し、オーバーレイアイコンを変更してもらう
                sendBroadcast(
                    Intent().apply {
                        action = OverlayService.ACTION_SWITCH_USB_DEBUG_STATUS
                    }
                )
            }
        }
    }

    /**
     * オーバーレイの設定画面を起動する
     */
    private fun launchOverlayStateSetting() {
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
            SetupStatusType.INTERNET_VIEW -> {
                setupInternetView()
            }
            SetupStatusType.OVERLAY_VIEW -> {
                setupOverlayView()
            }
            SetupStatusType.USB_DEBUG_VIEW -> {
                setupUsbDebugView()
            }
            SetupStatusType.NOTIFICATION_PERMISSION -> {
                requestNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
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
        if (OverlayStateRepository().isEnabled()) {
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
        startService(
            Intent(this, OverlayService::class.java).apply {
                putExtra(OverlayService.INTENT_ITEM_SELECTED_OVERLAY, selectSettingStateFlow.value.key)
            }
        )
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

    private fun sendCommandToOverlayService(setting: SelectedOverlayType) {
        sendBroadcast(
            Intent().apply {
                action = OverlayService.ACTION_SELECT_OVERLAY_SETTING
                putExtra(OverlayService.INTENT_ITEM_SELECTED_OVERLAY, setting.key)
            }
        )
    }

    /**
     * オーバーレイサービスを開始する
     */
    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
    }

    private fun setupOverlayView() {
        _overlayStateFlow.update{ OverlayStateRepository().load() }
        _selectSettingStateFlow.update { SelectedOverlayRepository().load() }
        proceedSetup(SetupStatusType.USB_DEBUG_VIEW)
    }

    private fun setupUsbDebugView() {
        _usbDebugStateFlow.update {
            if (UsbDebugStatusChecker.isUsbDebugEnabled(this)) {
                UsbDebugStateType.ON
            } else {
                UsbDebugStateType.OFF
            }
        }

        proceedSetup(SetupStatusType.NOTIFICATION_PERMISSION)
    }

    private fun setupInternetView() {
        _internetStateFlow.update {
            if (InternetStateRepository().isEnabled()) {
                InternetStateType.ON
            } else {
                InternetStateType.OFF
            }
        }
        proceedSetup(SetupStatusType.OVERLAY_VIEW)
    }

    @RequiresApi(Build.VERSION_CODES.O) // for RECEIVER_NOT_EXPORTED
    private fun setupOverlayReceiver() {
        mOverlayStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_SWITCH_OVERLAY_STATUS -> {
                        _overlayStateFlow.update {
                            if(intent.getStringExtra(KEY_OVERLAY_STATUS) == OverlayStateType.ON.key) {
                                OverlayStateType.ON
                            } else {
                                OverlayStateType.OFF
                            }
                        }
                        _selectSettingStateFlow.update {
                            intent.getStringExtra(KEY_SELECTED_OVERLAY)?.let {
                                SelectedOverlayType.fromKey(it)
                            } ?: SelectedOverlayType.USB_DEBUG
                        }
                    }
                    ACTION_SWITCH_INTERNET -> {
                        InternetStateRepository().setEnabled(
                            intent.getBooleanExtra(KEY_INTERNET_STATUS, false),
                            this@MainActivity
                        )
                    }
                }
            }
        }

        registerReceiver(
            mOverlayStatusReceiver,
            IntentFilter().apply {
                addAction(ACTION_SWITCH_OVERLAY_STATUS)
                addAction(ACTION_SWITCH_INTERNET)
            },
            RECEIVER_NOT_EXPORTED
        )

        proceedSetup(SetupStatusType.OVERLAY_PERMISSION)
    }

    @RequiresApi(Build.VERSION_CODES.O) // for RECEIVER_NOT_EXPORTED
    private fun setupWifiStateReceiver() {
        mWifiStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                    when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
                        WifiManager.WIFI_STATE_ENABLED -> {
                            _internetStateFlow.update { InternetStateType.ON }
                        }
                        WifiManager.WIFI_STATE_DISABLED -> {
                            _internetStateFlow.update { InternetStateType.OFF }
                        }
                    }
                    // オーバーレイサービスに Wi-Fi の状態が変化したことを通知し、オーバーレイアイコンを変更してもらう
                    sendBroadcast(
                        Intent().apply {
                            action = OverlayService.ACTION_SWITCH_INTERNET_STATUS
                        }
                    )
                }
            }
        }

        registerReceiver(
            mWifiStateReceiver,
            IntentFilter().apply { addAction(WifiManager.WIFI_STATE_CHANGED_ACTION) },
            RECEIVER_NOT_EXPORTED
        )

        proceedSetup(SetupStatusType.FINISH)
    }

    private fun finalizeReceiver()  {
        unregisterReceiver(mOverlayStatusReceiver)
        unregisterReceiver(mWifiStateReceiver)
    }
}
