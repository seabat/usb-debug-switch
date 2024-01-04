package dev.seabat.android.usbdebugswitch.compose.tutorial

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import dev.seabat.android.usbdebugswitch.compose.LoadingComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TutorialScreen(
    tutorialLoadedFlow: StateFlow<Boolean>,
) {
    val tutorialLoaded by tutorialLoadedFlow.collectAsState()

    if (tutorialLoaded) {
        Text(text = "Hello Tutorial!!")
    } else {
        LoadingComponent()
    }
}

@Preview
@Composable
fun TutorialScreenPreview() {
    TutorialScreen(
        MutableStateFlow(false)
    )
}