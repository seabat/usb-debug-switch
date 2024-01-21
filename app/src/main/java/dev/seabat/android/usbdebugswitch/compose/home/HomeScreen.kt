package dev.seabat.android.usbdebugswitch.compose.home

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.constants.InternetStateType
import dev.seabat.android.usbdebugswitch.constants.OverlayStateType
import dev.seabat.android.usbdebugswitch.constants.SelectedOverlayType
import dev.seabat.android.usbdebugswitch.constants.UsbDebugStateType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    internetStateFlow: StateFlow<InternetStateType>,
    overlayStateFlow: StateFlow<OverlayStateType>,
    usbDebugStateFlow: StateFlow<UsbDebugStateType>,
    selectedSettingStateFlow: StateFlow<SelectedOverlayType>,
    onInternetSwitch: (InternetStateType) -> Unit,
    onOverlaySwitch: () -> Unit,
    onUsbDebugSwitch: () -> Unit,
    onToggleSetting: (SelectedOverlayType) -> Unit,
    goTutorial: () -> Unit,
    goAppSetting: () -> Unit
) {
    var shouldOverlaySettingWarningShow by remember { mutableStateOf(false) }
    if (shouldOverlaySettingWarningShow) {
        OverlaySettingWarningDialog { shouldOverlaySettingWarningShow = false }
    }

    var shouldInternetSettingWarningShow by remember { mutableStateOf(false) }
    if (shouldInternetSettingWarningShow) {
        InternetSettingWarningDialog { shouldInternetSettingWarningShow = false }
    }

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
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        HomeContent(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(color = Color(0xFFF2F0F4))
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
            internetStateFlow = internetStateFlow,
            overlayStateFlow = overlayStateFlow,
            usbDebugStateFlow = usbDebugStateFlow,
            selectedSettingStateFlow = selectedSettingStateFlow,
            onInternetSettingWarning = { shouldInternetSettingWarningShow = true },
            onInternetSwitch = onInternetSwitch,
            onOverlaySettingWarning = { shouldOverlaySettingWarningShow = true },
            onOverlaySwitch = onOverlaySwitch,
            onUsbDebugSwitch = onUsbDebugSwitch,
            onToggleSetting = onToggleSetting,
            goTutorial = goTutorial,
            goAppSetting = goAppSetting
        )
    }
}


@Preview
@Composable
fun MainScreenPreview() {
    HomeScreen(
        MutableStateFlow(InternetStateType.OFF),
        MutableStateFlow(OverlayStateType.OFF),
        MutableStateFlow(UsbDebugStateType.OFF),
        MutableStateFlow(SelectedOverlayType.USB_DEBUG),
        onInternetSwitch = {},
        onOverlaySwitch = {},
        onUsbDebugSwitch = {},
        onToggleSetting = {},
        goTutorial = {},
        goAppSetting = {}
    )
}