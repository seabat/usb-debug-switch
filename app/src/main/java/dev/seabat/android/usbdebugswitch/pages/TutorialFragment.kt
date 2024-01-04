package dev.seabat.android.usbdebugswitch.pages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
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
                    tutorialLoadedFlow,
                    bitmapsFlow
                )
            }
        }
        loadTutorialState()
        return view
    }

    private fun loadTutorialState() {
        if (TutorialStateRepository().load() == TutorialStateType.DISPLAYED) {
            TutorialFragmentDirections.actionToHome()
        } else {
            _bitmapsFlow.update {
                arrayListOf(
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_app_on.png")
                    ),
                    BitmapFactory.decodeStream(
                        requireContext().assets.open("screenshot_app_off.png")
                    )
                )
            }
        }
        _tutorialLoadedFlow.update { true }
    }
}