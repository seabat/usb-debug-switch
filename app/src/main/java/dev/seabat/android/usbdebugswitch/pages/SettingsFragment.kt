package dev.seabat.android.usbdebugswitch.pages

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.compose.setting.SettingScreen
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext()).apply {
            setContent {
                SettingScreen(
                    launchBrowser = {},
                    goLicense = {
                        findNavController().navigate(SettingsFragmentDirections.actionToLicense())
                    },
                    goBack = {
                        lifecycleScope.launch {
                            findNavController().popBackStack()
                        }
                    }
                )
            }
        }
        return view
    }
}