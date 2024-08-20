package dev.seabat.android.usbdebugswitch.pages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import dev.seabat.android.usbdebugswitch.compose.license.LicenseScreen
import dev.seabat.android.usbdebugswitch.utils.LibraryLicenseList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LicenseFragment : Fragment() {
    private val _licensesLoadedStateFlow = MutableStateFlow(false)
    private val licenseLoadedStateFlow = _licensesLoadedStateFlow.asStateFlow()

    private val _licensesStateFlow = MutableStateFlow(LibraryLicenseList(arrayListOf()))
    private val licensesStateFlow = _licensesStateFlow.asStateFlow()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext()).apply {
            setContent {
                LicenseScreen(
                    licensesStateFlow,
                    licenseLoadedStateFlow
                ) {
                    lifecycleScope.launch {
                        findNavController().popBackStack()
                    }
                }
            }
        }
        licenseInfo()
        return view
    }

    private fun licenseInfo() {
        lifecycleScope.launch {
            _licensesStateFlow.update {
                LibraryLicenseList.create(requireContext())
            }
            _licensesLoadedStateFlow.update { true }
        }
    }
}