package dev.seabat.android.usbdebugswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
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
        const val REQUEST_OVERLAY_PERMISSION: Int = 1;
        const val REQUEST_APPLICATION_DEVELOPMENT_SETTINGS = 20
        const val TAG_GOTO_OVERLAY_SETTING = "TAG_GOTO_OVERLAY_SETTING";
        const val ACTION_SWITCH_OVERLAY_STATUS = "ACTION_SWITCH_OVERLAY_STATUS"
        const val KEY_OVERLAY_STATUS = "KEY_OVERLAY_STATUS"
    }

    private lateinit var mReceiver: BroadcastReceiver


    enum class SetupStatusType {
        START,
        NOTIFICATION_PERMISSION, // 通知パーミッションのセットアップ
        OVERLAY, // オーバーレイ表示領域のセットアップ
        USB_DEBUG,
    }

    private var setupStatus = SetupStatusType.START

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            proceedSetup(SetupStatusType.NOTIFICATION_PERMISSION)
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
        setUpOverlayReceiver()

        proceedSetup(SetupStatusType.START)
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
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"))
        // 設定画面を起動する
        // 設定->アプリ->歯車アイコン->他のアプリの上に重ねて表示

        startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_OVERLAY_PERMISSION -> {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O ||
                    Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1) {
                    return
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    if(Settings.canDrawOverlays(this)) {
                        Toast.makeText(
                            this@MainActivity,
                            "Success to ACTION_MANAGE_OVERLAY_PERMISSION Permission",
                            Toast.LENGTH_SHORT
                        ).show()
                        tryToStartOverlayService()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "ACTION_MANAGE_OVERLAY_PERMISSION Permission Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            REQUEST_APPLICATION_DEVELOPMENT_SETTINGS -> {
                setUpUsbDebug()
                Intent().let {
                    it.action = OverlayService.ACTION_SWITCH_USB_DEBUG_STATUS
                    sendBroadcast(it)
                }
            }
        }
    }

    private fun proceedSetup(setupStatus: SetupStatusType) {
        this.setupStatus = setupStatus
        Log.i("UsbDebugSwitch", "Setup status: $setupStatus")
        when(setupStatus) {
            SetupStatusType.START -> {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            SetupStatusType.NOTIFICATION_PERMISSION -> {
                setUpOverlay()
            }
            SetupStatusType.OVERLAY -> {
                setUpUsbDebug()
            }
            SetupStatusType.USB_DEBUG -> {
                // Do nothing
            }
        }
    }

    /**
     * オーバーレイ preference を初期化する
     *
     * Preference に "ON" が格納されている場合は、オーバーレイサービスの開始を試みる。
     */
    private fun setUpOverlay() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPref.getString(
            "pref_setting_overlay",
            getString(R.string.setting_overlay_off)
        ).let { statusString ->
            _overlayStateFlow.update {
                statusString ?: getString(R.string.setting_overlay_off)
            }
            if (statusString == getString(R.string.setting_overlay_on)) {
                tryToStartOverlayService()
            } else {
                proceedSetup(SetupStatusType.OVERLAY)
            }
        }
    }


    /**
     * オーバーレイサービスを開始を試みる
     */
    private fun tryToStartOverlayService(){
        if (isEnabledOverlayPermission() ) {
            val intent = Intent(this, OverlayService::class.java)
            startService(intent)
            proceedSetup(SetupStatusType.OVERLAY)
        } else {
            // オーバーレイ権限がない場合、ダイアログを表示
            MessageDialogFragment.newInstance(getString(R.string.dialog_msg_goto_overlay_setting))
                .show(this.supportFragmentManager, MainActivity.TAG_GOTO_OVERLAY_SETTING)
            disableOverlayPreference()
        }
    }


    /**
     * オーバーレイ許可がONになっているかを判定
     */
    private fun isEnabledOverlayPermission(): Boolean {
        return OverlayPermissionChecker.isEnabled(this)
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


    private fun setUpUsbDebug() {
        _usbDebugStateFlow.update {
            if (UsbDebugStatusChecker.isUsbDebugEnabled(this)) {
                getString(R.string.setting_usb_debug_on)
            } else {
                getString(R.string.setting_usb_debug_off)
            }
        }

        proceedSetup(SetupStatusType.USB_DEBUG)
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

        IntentFilter().let {
            it.addAction(ACTION_SWITCH_OVERLAY_STATUS)
            registerReceiver(mReceiver, it)
        }
    }

    private fun finalizeReceiver()  {
        unregisterReceiver(mReceiver)
    }
}
