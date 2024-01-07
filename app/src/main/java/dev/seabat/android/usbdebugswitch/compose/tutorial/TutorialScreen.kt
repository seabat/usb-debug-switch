package dev.seabat.android.usbdebugswitch.compose.tutorial

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.compose.LoadingComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    tutorialLoadedFlow: StateFlow<Boolean>,
    bitmapsFlow: StateFlow<MutableList<Bitmap>>,
    onClose: () -> Unit
) {
    val tutorialLoaded by tutorialLoadedFlow.collectAsState()
    val bitmaps by bitmapsFlow.collectAsState()

    if (tutorialLoaded) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF2F0F4)),
                    title = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = stringResource(id = R.string.tutorial_title),
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                )
            }
        ) { contentPadding ->
            TutorialContent(
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
                    .background(color = Color(0xFFF2F0F4))
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .verticalScroll(rememberScrollState()),
                bitmaps,
                onClose
            )
        }
    } else {
        LoadingComponent()
    }
}


@Preview
@Composable
fun TutorialScreenPreview() {
    val context = LocalContext.current
    TutorialScreen(
        MutableStateFlow(false),
        MutableStateFlow(
            arrayListOf(
                BitmapFactory.decodeStream(
                    context.assets.open("screenshot_app_off.png")
                )
            )
        )
    ) { /* Do nothing */ }
}