package dev.seabat.android.usbdebugswitch.pages

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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = ComposeView(requireContext()).apply {
            setContent {
                TutorialScreen(tutorialLoadedFlow)
            }
        }
        loadTutorialState()
        return view
    }

    private fun loadTutorialState() {
        if (TutorialStateRepository().load() == TutorialStateType.DISPLAYED) {
            TutorialFragmentDirections.actionToHome()
        } else {

        }
        _tutorialLoadedFlow.update { true }
    }
}