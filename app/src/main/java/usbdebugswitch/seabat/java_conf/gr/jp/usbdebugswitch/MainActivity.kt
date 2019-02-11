package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast


class MainActivity : AppCompatActivity(), MessageDialogFragment.OnClickListener{

    companion object {
        const val TAG = "MainActivity"
        private val DEBUG = BuildConfig.DEBUG
        const val REQUEST_OVERLAY_PERMISSION: Int = 1;
        const val TAG_GOTO_OVERLAY_SETTING = "TAG_GOTO_OVERLAY_SETTING";
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if(DEBUG) Log.d(TAG, "[${taskId}] onCreate")
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        addFragment()
    }

    /**
     * Fragment を追加
     */
    private fun addFragment() {
        val fm = supportFragmentManager

        if (fm.findFragmentById(R.id.main_frame_layout) == null) {
            val fragment = MainFragment()
            fm.beginTransaction().add(R.id.main_frame_layout, fragment).commit()
        }
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

        startActivityForResult(intent, MainActivity.REQUEST_OVERLAY_PERMISSION)
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
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "ACTION_MANAGE_OVERLAY_PERMISSION Permission Denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                (supportFragmentManager.findFragmentById(R.id.main_frame_layout) as MainFragment).tryToStartOverlayService()
            }
        }
    }
}
