package dev.seabat.android.usbdebugswitch.pages

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.compose.home.HomeScreen
import dev.seabat.android.usbdebugswitch.constants.InternetStateType
import dev.seabat.android.usbdebugswitch.constants.OverlayStateType
import dev.seabat.android.usbdebugswitch.constants.SelectedOverlayType
import dev.seabat.android.usbdebugswitch.constants.TutorialStateType
import dev.seabat.android.usbdebugswitch.constants.UsbDebugStateType
import dev.seabat.android.usbdebugswitch.dialog.PermissionWarningDialog
import dev.seabat.android.usbdebugswitch.repositories.InternetStateRepository
import dev.seabat.android.usbdebugswitch.repositories.OverlayStateRepository
import dev.seabat.android.usbdebugswitch.repositories.SelectedOverlayRepository
import dev.seabat.android.usbdebugswitch.repositories.TutorialStateRepository
import dev.seabat.android.usbdebugswitch.services.OverlayService
import dev.seabat.android.usbdebugswitch.utils.CheckNotificationPermission
import dev.seabat.android.usbdebugswitch.utils.CheckOverlayPermission
import dev.seabat.android.usbdebugswitch.utils.DeveloperOptionsLauncher
import dev.seabat.android.usbdebugswitch.utils.UsbDebugStatusChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    companion object {
        const val TAG_GOTO_OVERLAY_SETTING = "TAG_GOTO_OVERLAY_SETTING"
        const val TAG_GOTO_APP_SETTING = "TAG_GOTO_APP_SETTING"
        const val ACTION_SWITCH_OVERLAY_STATUS = "ACTION_SWITCH_OVERLAY_STATUS"
        const val ACTION_SWITCH_INTERNET = "ACTION_SWITCH_INTERNET"
        const val ACTION_LAUNCH_DEVELOPER_OPTIONS = "ACTION_LAUNCH_DEVELOPER_OPTIONS"
        const val KEY_OVERLAY_STATUS = "KEY_OVERLAY_STATUS"
        const val KEY_SELECTED_OVERLAY = "KEY_SELECTED_OVERLAY"
        const val KEY_INTERNET_STATUS = "KEY_INTERNET_STATUS"
    }

    private lateinit var mOverlayStatusReceiver: BroadcastReceiver
    private lateinit var mWifiStateReceiver: BroadcastReceiver

    enum class SetupStatusType {
        READY,
        INTERNET_VIEW,
        OVERLAY_VIEW,
        USB_DEBUG_VIEW, // オーバーレイ表示領域のセットアップ
        NOTIFICATION_PERMISSION,
        OVERLAY_RECEIVER,
        OVERLAY_PERMISSION,
        OVERLAY_SERVICE, // 通知パーミッションのセットアップ
        WIFI_STATE_RECEIVER,
        FINISH
    }

    private var setupStatus = SetupStatusType.READY

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            proceedSetup(next = SetupStatusType.OVERLAY_RECEIVER)
        } else {
            Toast.makeText(
                requireActivity(),
                "Please grant Post Notification Permission",
                Toast.LENGTH_SHORT
            )
        }
    }
    private val developerOptionsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        updateUsbDebugView()
        // オーバーレイサービスに「開発者向けオプション」画面から戻ったことを通知し、オーバーレイアイコンを変更してもらう
        requireContext().sendBroadcast(
            Intent().apply {
                action = OverlayService.ACTION_SWITCH_USB_DEBUG_STATUS
            }
        )
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext()).apply {
            setContent {
                HomeScreen(
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
                        DeveloperOptionsLauncher.startActivityForResult { intent ->
                            developerOptionsLauncher.launch(intent)
                        }
                    },
                    onInternetSwitch = {
                        InternetStateRepository().setEnabled(
                            it == InternetStateType.ON,
                            requireActivity()
                        )
                    },
                    onToggleSetting = {
                        sendOverlayTypeToOverlayService(it)
                    },
                    goTutorial = {
                        lifecycleScope.launch {
                            TutorialStateRepository().save(TutorialStateType.NOT_DISPLAYED)
                            findNavController().navigate(HomeFragmentDirections.actionToTutorial())
                        }
                    },
                    goAppSetting = {
                        lifecycleScope.launch {
                            findNavController().navigate(HomeFragmentDirections.actionToSetting())
                        }
                    }
                )
            }
        }
        parentFragmentManager.setFragmentResultListener(
            TAG_GOTO_OVERLAY_SETTING,
            this
        ) { _, _ ->
            launchOverlayStateSetting()
        }

        parentFragmentManager.setFragmentResultListener(
            TAG_GOTO_APP_SETTING,
            this
        ) { _, _ ->
            launchAppSetting()
        }

        // セットアップ開始
        proceedSetup(SetupStatusType.INTERNET_VIEW)

        return view
    }

    override fun onDestroy() {
        unRegisterReceivers()
        super.onDestroy()
    }

    /**
     * オーバーレイの設定画面を起動する
     */
    private fun launchOverlayStateSetting() {
        // 設定画面を起動する
        // 設定->アプリ->歯車アイコン->他のアプリの上に重ねて表示
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${requireContext().packageName}")
        )
        startActivity(intent)

        appOpsManager = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        appOpsManager?.startWatchingMode(
            AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
            requireContext().packageName,
            object : AppOpsManager.OnOpChangedListener {
                override fun onOpChanged(op: String?, packageName: String?) {
                    appOpsManager?.stopWatchingMode(this) // 監視を止める
                    CheckOverlayPermission(requireContext())(
                        enabled = { proceedSetup(next = SetupStatusType.OVERLAY_SERVICE) },
                        disabled = { proceedSetup(next = SetupStatusType.WIFI_STATE_RECEIVER) }
                    )
                }
            }
        )
    }

    /**
     * 設定アプリの Usb Debug Switch 設定画面を起動する
     */
    private fun launchAppSetting() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.parse("package:dev.seabat.android.usbdebugswitch")
        }
        startActivity(intent)
    }

    /**
     * 初期化を進行させる
     */
    private fun proceedSetup(next: SetupStatusType) {
        this.setupStatus = next
        Log.i("UsbDebugSwitch", "Setup status: $next")
        when (setupStatus) {
            SetupStatusType.READY -> {
                // Do nothing
            }
            SetupStatusType.INTERNET_VIEW -> {
                lifecycleScope.launch {
                    setupInternetView()
                }
            }
            SetupStatusType.OVERLAY_VIEW -> {
                lifecycleScope.launch {
                    setupOverlayView()
                }
            }
            SetupStatusType.USB_DEBUG_VIEW -> {
                lifecycleScope.launch {
                    setupUsbDebugView()
                }
            }
            SetupStatusType.NOTIFICATION_PERMISSION -> {
                lifecycleScope.launch {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestNotificationPermissionLauncher.launch(
                            android.Manifest.permission.POST_NOTIFICATIONS
                        )
                    } else {
                        proceedSetup(next = SetupStatusType.OVERLAY_RECEIVER)
                    }
                }
            }
            SetupStatusType.OVERLAY_RECEIVER -> {
                lifecycleScope.launch {
                    registerReceiverForOverlayService()
                }
            }
            SetupStatusType.OVERLAY_PERMISSION -> {
                lifecycleScope.launch {
                    setupOverlayPermission()
                }
            }
            SetupStatusType.OVERLAY_SERVICE -> {
                lifecycleScope.launch {
                    startOverlayService()
                }
            }
            SetupStatusType.WIFI_STATE_RECEIVER -> {
                lifecycleScope.launch {
                    registerWifiStateReceiver()
                }
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
            CheckOverlayPermission(requireContext())(
                enabled = {
                    proceedSetup(next = SetupStatusType.OVERLAY_SERVICE)
                },
                disabled = {
                    // オーバーレイ権限がない場合、ダイアログを表示
                    PermissionWarningDialog
                        .newInstance(
                            getString(R.string.dialog_msg_goto_overlay_setting),
                            TAG_GOTO_OVERLAY_SETTING
                        )
                        .show(parentFragmentManager, TAG_GOTO_OVERLAY_SETTING)
                }
            )
        } else {
            proceedSetup(next = SetupStatusType.WIFI_STATE_RECEIVER)
        }
    }

    /**
     * OverlayService を開始する
     */
    private fun startOverlayService() {
        requireContext().startService(
            Intent(requireContext(), OverlayService::class.java).apply {
                putExtra(
                    OverlayService.INTENT_ITEM_SELECTED_OVERLAY,
                    selectSettingStateFlow.value.key
                )
            }
        )
        proceedSetup(next = SetupStatusType.WIFI_STATE_RECEIVER)
    }

    /**
     * OverlayService の開始を試みる
     */
    private fun tryToStartOverlayService() {
        // 通知パーミッションをチェック
        CheckNotificationPermission(requireContext())(
            enabled = {
                // オーバーレイ権限をチェック
                CheckOverlayPermission(requireContext())(
                    enabled = {
                        // オーバーレイサービスを開始
                        val intent = Intent(requireContext(), OverlayService::class.java)
                        requireContext().startService(intent)
                    },
                    disabled = {
                        // オーバーレイ権限がない場合、ダイアログを表示
                        PermissionWarningDialog
                            .newInstance(
                                getString(R.string.dialog_msg_goto_overlay_setting),
                                TAG_GOTO_OVERLAY_SETTING
                            )
                            .show(parentFragmentManager, TAG_GOTO_OVERLAY_SETTING)
                    }
                )
            },
            disabled = {
                // パーミッション警告ダイアログを表示
                PermissionWarningDialog
                    .newInstance(
                        getString(R.string.notification_permission_dialog_message),
                        TAG_GOTO_APP_SETTING
                    )
                    .show(parentFragmentManager, TAG_GOTO_APP_SETTING)
            }
        )
    }

    /**
     * OverlayService にオーバーレイのタイプを送信する
     */
    private fun sendOverlayTypeToOverlayService(setting: SelectedOverlayType) {
        requireContext().sendBroadcast(
            Intent().apply {
                action = OverlayService.ACTION_SELECT_OVERLAY_SETTING
                putExtra(OverlayService.INTENT_ITEM_SELECTED_OVERLAY, setting.key)
            }
        )
    }

    /**
     * OverlayService を停止する
     */
    private fun stopOverlayService() {
        requireContext().stopService(Intent(requireContext(), OverlayService::class.java))
    }

    /**
     * オーバーレイ設定表示を初期化
     */
    private fun setupOverlayView() {
        _overlayStateFlow.update { OverlayStateRepository().load() }
        _selectSettingStateFlow.update { SelectedOverlayRepository().load() }
        proceedSetup(SetupStatusType.USB_DEBUG_VIEW)
    }

    /**
     * USB デバッグ状態を表示
     */
    private fun updateUsbDebugView() {
        _usbDebugStateFlow.update {
            if (UsbDebugStatusChecker.isUsbDebugEnabled(requireContext())) {
                UsbDebugStateType.ON
            } else {
                UsbDebugStateType.OFF
            }
        }
    }

    /**
     * USB デバッグ表示を初期化
     */
    private fun setupUsbDebugView() {
        updateUsbDebugView()
        proceedSetup(SetupStatusType.NOTIFICATION_PERMISSION)
    }

    /**
     * インターネット接続表示を初期化
     */
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

    private fun registerReceiverForOverlayService() {
        mOverlayStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ACTION_SWITCH_OVERLAY_STATUS -> {
                        _overlayStateFlow.update {
                            if (intent.getStringExtra(KEY_OVERLAY_STATUS) ==
                                OverlayStateType.ON.key
                            ) {
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
                            requireActivity()
                        )
                    }
                    ACTION_LAUNCH_DEVELOPER_OPTIONS -> {
                        DeveloperOptionsLauncher.startActivityForResult { resultIntent ->
                            developerOptionsLauncher.launch(resultIntent)
                        }
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                mOverlayStatusReceiver,
                IntentFilter().apply {
                    addAction(ACTION_SWITCH_OVERLAY_STATUS)
                    addAction(ACTION_SWITCH_INTERNET)
                    addAction(ACTION_LAUNCH_DEVELOPER_OPTIONS)
                },
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            requireContext().registerReceiver(
                mOverlayStatusReceiver,
                IntentFilter().apply {
                    addAction(ACTION_SWITCH_OVERLAY_STATUS)
                    addAction(ACTION_SWITCH_INTERNET)
                    addAction(ACTION_LAUNCH_DEVELOPER_OPTIONS)
                }
            )
        }

        proceedSetup(SetupStatusType.OVERLAY_PERMISSION)
    }

    private fun registerWifiStateReceiver() {
        mWifiStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == WifiManager.WIFI_STATE_CHANGED_ACTION) {
                    when (
                        intent.getIntExtra(
                            WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_UNKNOWN
                        )
                    ) {
                        WifiManager.WIFI_STATE_ENABLED -> {
                            _internetStateFlow.update { InternetStateType.ON }
                        }
                        WifiManager.WIFI_STATE_DISABLED -> {
                            _internetStateFlow.update { InternetStateType.OFF }
                        }
                    }
                    // オーバーレイサービスに Wi-Fi の状態が変化したことを通知し、オーバーレイアイコンを変更してもらう
                    requireContext().sendBroadcast(
                        Intent().apply {
                            action = OverlayService.ACTION_SWITCH_INTERNET_STATUS
                        }
                    )
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                mWifiStateReceiver,
                IntentFilter().apply { addAction(WifiManager.WIFI_STATE_CHANGED_ACTION) },
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            requireContext().registerReceiver(
                mWifiStateReceiver,
                IntentFilter().apply { addAction(WifiManager.WIFI_STATE_CHANGED_ACTION) }
            )
        }

        proceedSetup(SetupStatusType.FINISH)
    }

    private fun unRegisterReceivers() {
        requireContext().unregisterReceiver(mOverlayStatusReceiver)
        requireContext().unregisterReceiver(mWifiStateReceiver)
    }
}
