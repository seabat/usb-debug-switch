package dev.seabat.android.usbdebugswitch.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dev.seabat.android.usbdebugswitch.compose.tutorial.TutorialScreen
import dev.seabat.android.usbdebugswitch.constants.TutorialStateType
import dev.seabat.android.usbdebugswitch.repositories.TutorialStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TutorialFragment : Fragment() {

    private val _tutorialLoadedFlow = MutableStateFlow(false)
    private val tutorialLoadedFlow = _tutorialLoadedFlow.asStateFlow()

    private val _bitmapsFlow = MutableStateFlow<MutableList<Bitmap>>(arrayListOf())
    private val bitmapsFlow = _bitmapsFlow.asStateFlow()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext()).apply {
            setContent {
                TutorialScreen(
                    tutorialLoadedFlow = tutorialLoadedFlow,
                    bitmapsFlow = bitmapsFlow,
                    onClose = {
                        findNavController().navigate(TutorialFragmentDirections.actionToHome())
                    }
                )
            }
        }
        loadTutorialState()
        return view
    }

    private fun loadTutorialState() {
        if (TutorialStateRepository().load() == TutorialStateType.DISPLAYED) {
            findNavController().navigate(TutorialFragmentDirections.actionToHome())
        } else {
            _bitmapsFlow.update {
                arrayListOf(
                    // 1
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_developer_option.png")
                    ),
                    // 2
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_notification_permission.png")
                    ),
                    // 3
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_enable_overlay.png")
                    ),
                    // 4
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_select_usb_debug_switch.png")
                    ),
                    // 5
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_usb_debug_switch_overlay_setting.png")
                    ),
                    // 6
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_app_off.png")
                    ),
                    // 7
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_usb_debug_setting_off.png")
                    ),
                    // 8
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_usb_debug_setting_dialog.png")
                    ),
                    // 9
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_usb_debug_setting_on.png")
                    ),
                    // 10
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_app_on.png")
                    ),
                    // 11
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_home_on_top.png")
                    ),
                )
            }
            TutorialStateRepository().save(TutorialStateType.DISPLAYED)
        }
        _tutorialLoadedFlow.update { true }
    }
}