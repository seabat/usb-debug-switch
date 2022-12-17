package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.annotation.TargetApi
import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Build
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.OverlayService.Companion.ACTION_SWITCH_USB_DEBUG_STATUS
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils.SettingsLauncher
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils.OverlayPermissionChecker
import usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch.utils.UsbDebugStatusChecker

class MainFragment : PreferenceFragmentCompat() {

    companion object {
        const val TAG = "MainFragment"
        private val DEBUG = BuildConfig.DEBUG
        const val REQUEST_APPLICATION_DEVELOPMENT_SETTINGS = 20
        const val ACTION_SWITCH_OVERLAY_STATUS = "ACTION_SWITCH_OVERLAY_STATUS"
        const val KEY_OVERLAY_STATUS = "KEY_OVERLAY_STATUS"
    }

    private lateinit var mChangePrefListener: SharedPreferences.OnSharedPreferenceChangeListener

    private lateinit var mReceiver: BroadcastReceiver

    private var OVERLAY_ON: String = ""
    private var OVERLAY_OFF: String= ""
    private var GOTO_OVERLAY_SETTING: String= ""
    private var USB_DEBUG_ON: String= ""
    private var USB_DEBUG_OFF: String= ""


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onCreatePreferences(p0: Bundle?, p1: String?) {

          // Load the appinfo_preferences from an XML resource
        addPreferencesFromResource(R.xml.setting_pref)
        activity?.let {
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            setUpOverlay(sharedPref)

            setUpUsbDebug()

            setUpAutoBoot()

            setUpChangePrefListener()
        }
    }


    /**
     * 文字列リソースから文字列を初期化するする
     *
     * Fragment.getString() は　context が必要。
     * context は onAttach() のあとじゃないと使えない。
     *
     */
    private fun setUpDefineFromResource() {
        OVERLAY_ON = getString(R.string.setting_overlay_on)
        OVERLAY_OFF = getString(R.string.setting_overlay_off)
        GOTO_OVERLAY_SETTING = getString(R.string.dialog_msg_goto_overlay_setting)
        USB_DEBUG_ON = getString(R.string.setting_usb_debug_on)
        USB_DEBUG_OFF = getString(R.string.setting_usb_debug_off)
    }


    /**
     * オーバーレイ preference を初期化する
     *
     * Preference に "ON" が格納されている場合は、オーバーレイサービスの開始を試みる。
     */
    private fun setUpOverlay(sharedPref: SharedPreferences) {
        findPreference("pref_setting_overlay").let { overlayStatus ->
            sharedPref.getString("pref_setting_overlay", OVERLAY_OFF).let { statusString ->
                overlayStatus.summary = statusString
                if (statusString == OVERLAY_ON) {
                    tryToStartOverlayService()
                }
            }
        }
    }


    /**
     * オーバーレイサービスを開始を試みる
     */
    fun tryToStartOverlayService(){
        if (isEnabledOverlayPermission() ) {
            val intent = Intent(activity, OverlayService::class.java)
            activity?.startService(intent)
        } else {
            disableOverlayPreference()
        }
    }


    /**
     * オーバーレイ許可がONになっているかを判定し、
     * OFFの場合はオーバーレイ許可を尋ねるダイアログを表示する。
     */
    private fun isEnabledOverlayPermission(): Boolean {
        if(OverlayPermissionChecker.isEnabled(requireContext())) {
            return true
        }

        // オーバーレイ権限がない場合、ダイアログを表示
        MessageDialogFragment.newInstance(GOTO_OVERLAY_SETTING)
            .show(requireActivity().supportFragmentManager, MainActivity.TAG_GOTO_OVERLAY_SETTING)

        return false
    }


    /**
     * オーバーレイ preference を無効にする
     */
    private fun disableOverlayPreference() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPref.edit().let {editor ->
            editor.putString("pref_setting_overlay", OVERLAY_OFF)
            editor.commit() // commit を忘れずに！
        }

        (findPreference("pref_setting_overlay") as ListPreference).let { overlayStatus ->
            overlayStatus.summary = OVERLAY_OFF
            overlayStatus.setValueIndex(1);
        }
    }

    /**
     * オーバーレイ preference を有効にする
     */
    private fun enableOverlayPreference() {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
        sharedPref.edit().let {editor ->
            editor.putString("pref_setting_overlay", OVERLAY_ON)
            editor.commit() // commit を忘れずに！
        }

        (findPreference("pref_setting_overlay") as ListPreference).let { overlayStatus ->
            overlayStatus.summary = OVERLAY_ON
            overlayStatus.setValueIndex(0);
        }
    }


    /**
     * オーバーレイサービスを開始する
     */
    private fun stopOverlayService() {
        val intent = Intent(activity, OverlayService::class.java)
        activity?.stopService(intent)
    }


    private fun setUpUsbDebug() {
        //USBデバッグ設定を Preference に反映
        findPreference("pref_setting_usb_debug").let {usbDebugStatus ->
            usbDebugStatus.summary =
                    if (UsbDebugStatusChecker.isUsbDebugEnabled(requireActivity())) {
                        USB_DEBUG_ON
                    } else {
                        USB_DEBUG_OFF
                    }

            usbDebugStatus.setOnPreferenceClickListener (object : Preference.OnPreferenceClickListener {
                override fun onPreferenceClick(p: Preference?): Boolean {
                    val key =  p?.key
                    if (key == "pref_setting_usb_debug") {
                        SettingsLauncher.startForResultFromFragment(this@MainFragment)
                          // 設定画面を起動する
                        return true
                    }
                    return false
                }
            })
        }
    }


    private fun setUpAutoBoot() {
        //TODO: サービス自動起動のON/OFF
//        //自動起動設定を CheckBoxPreference に反映
//        val usbDebugPref = findPreference("pref_enable_receive_boot_complete") as CheckBoxPreference
//        if(DEBUG) Log.d(TAG, "USB Debug = ${usbDebugPref.isChecked}")
    }

    private fun setUpChangePrefListener() {
        mChangePrefListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key){
                "pref_setting_overlay" -> {
                    val overlaySetting = sharedPreferences.getString("pref_setting_overlay", OVERLAY_OFF)
                    if (overlaySetting == OVERLAY_ON) {
                        tryToStartOverlayService()
                    } else {
                        stopOverlayService()
                    }
                }
                "pref_setting_usb_debug" -> {
                    // Do nothing
                }
                "pref_enable_receive_boot_complete" -> {
                    sharedPreferences.getBoolean(key, false).let {
                        //TODO: サービス自動起動のON/OFF
//                        BootReceiverSwitch.switch(context!!.applicationContext,it)
                    }
                }
            }
        }
        PreferenceManager.getDefaultSharedPreferences(activity).registerOnSharedPreferenceChangeListener(mChangePrefListener)
    }


    override fun onAttach(context: Context) {
        if(DEBUG) Log.d(TAG, "onAttach")
        super.onAttach(context)

        setUpDefineFromResource()

        setUpOverlayReceiver()
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
            activity?.registerReceiver(mReceiver, it)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onStart() {
        if(DEBUG) Log.d(TAG, "onStart")
        super.onStart()
    }


    override fun onResume() {
        if(DEBUG) Log.d(TAG, "onResume")
        super.onResume()
    }


    override fun onStop() {
        if(DEBUG) Log.d(TAG, "onStop")
        super.onStop()
    }


    override fun onDetach() {
        super.onDetach()
        finalizeReceiver()
    }


    private fun finalizeReceiver()  {
        activity?.unregisterReceiver(mReceiver)
    }


    override fun onDestroy() {
        if(DEBUG) Log.d(TAG, "onDestroy")
        PreferenceManager.getDefaultSharedPreferences(activity).unregisterOnSharedPreferenceChangeListener(mChangePrefListener)
        super.onDestroy()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            MainFragment.REQUEST_APPLICATION_DEVELOPMENT_SETTINGS -> {
                setUpUsbDebug()
                Intent().let {
                    it.action = ACTION_SWITCH_USB_DEBUG_STATUS
                    activity?.sendBroadcast(it)
                }
            }
        }
    }
}// Required empty public constructor


