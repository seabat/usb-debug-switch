package dev.seabat.android.usbdebugswitch.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
fun MainScreen(
    internetStateFlow: StateFlow<InternetStateType>,
    overlayStateFlow: StateFlow<OverlayStateType>,
    usbDebugStateFlow: StateFlow<UsbDebugStateType>,
    selectedSettingStateFlow: StateFlow<SelectedOverlayType>,
    onInternetSwitch: (InternetStateType) -> Unit,
    onOverlaySwitch: () -> Unit,
    onUsbDebugSwitch: () -> Unit,
    onToggleSetting: (SelectedOverlayType) -> Unit
) {
    val internetState by internetStateFlow.collectAsState()
    val overlayState by overlayStateFlow.collectAsState()
    val usbDebugState by usbDebugStateFlow.collectAsState()
    val selectedSettingState by selectedSettingStateFlow.collectAsState()

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .background(color = Color(0xFFF2F0F4)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                modifier = Modifier
                    .padding(vertical = 20.dp)
                    .size(180.dp),
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name)
            )

            OverlaySettingCard(
                title =  stringResource(id = R.string.title_setting_overlay),
                overlayState =  overlayState,
                selectedSettingState = selectedSettingState,
                onSwitch = onOverlaySwitch,
                onToggleSetting = onToggleSetting
            )

            SettingCard(
                title =  stringResource(id = R.string.title_setting_usb_debug),
                onOff =  usbDebugState.key,
                onSwitch = { onUsbDebugSwitch() }
            )

            SettingCard(
                title =  stringResource(id = R.string.title_setting_internet),
                onOff =  internetState.key,
                onSwitch = {
                    onInternetSwitch(
                        if (it == "on") {
                            InternetStateType.OFF
                        } else {
                            InternetStateType.ON
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun OverlaySettingCard(
    title: String,
    overlayState: OverlayStateType,
    selectedSettingState: SelectedOverlayType,
    onSwitch: () -> Unit,
    onToggleSetting: (SelectedOverlayType) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFBEFF6)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1.0f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )

                val radioOptions = listOf(
                    stringResource(id = R.string.title_setting_usb_debug),
                    stringResource(id = R.string.title_setting_internet)
                )

                radioOptions.forEach { settingName ->
                    Row(modifier = Modifier.height(30.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF75565c)),
                            selected = settingName == when(selectedSettingState) {
                                SelectedOverlayType.USB_DEBUG -> radioOptions[0]
                                SelectedOverlayType.INTERNET -> radioOptions[1]
                            },
                            onClick = {
                                onToggleSetting(
                                    when(settingName) {
                                        radioOptions[0] -> SelectedOverlayType.USB_DEBUG
                                        radioOptions[1] -> SelectedOverlayType.INTERNET
                                        else -> SelectedOverlayType.USB_DEBUG
                                    }
                                )
                            }
                        )
                        Text(
                            text = settingName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Switch(
                checked = overlayState ==  OverlayStateType.ON,
                onCheckedChange = { onSwitch() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFFFFFF),
                    checkedTrackColor = Color(0xFF75565c)
                )
            )
        }
    }
}

@Composable
fun SettingCard(title: String, onOff: String, onSwitch: (String) -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFBEFF6)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .height(100.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1.0f),
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
            Switch(
                checked = onOff == "on",
                onCheckedChange = { onSwitch(onOff) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFFFFFFFF),
                    checkedTrackColor = Color(0xFF75565c)
                )
            )
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(
        MutableStateFlow(InternetStateType.OFF),
        MutableStateFlow(OverlayStateType.OFF),
        MutableStateFlow(UsbDebugStateType.OFF),
        MutableStateFlow(SelectedOverlayType.USB_DEBUG),
        onInternetSwitch = {},
        onOverlaySwitch = {},
        onUsbDebugSwitch = {},
        onToggleSetting = {}
    )
}