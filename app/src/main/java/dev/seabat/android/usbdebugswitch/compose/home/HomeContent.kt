package dev.seabat.android.usbdebugswitch.compose.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
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
    val internetState by internetStateFlow.collectAsState()
    val overlayState by overlayStateFlow.collectAsState()
    val usbDebugState by usbDebugStateFlow.collectAsState()
    val selectedSettingState by selectedSettingStateFlow.collectAsState()

    Box(modifier = modifier) {
        // 使い方
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .border(
                        width = 2.dp,
                        color = Color(0xFF75565c),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { goTutorial() }
                    .padding(top = 5.dp, start = 10.dp, end = 10.dp, bottom = 5.dp),
                text = stringResource(id = R.string.tutorial),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF75565c)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // アプリアイコン
            Image(
                modifier = Modifier.size(120.dp).clip(CircleShape),
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = stringResource(id = R.string.app_name),
            )


            // スイッチを常に表示
            OverlaySettingCard(
                overlayState =  overlayState,
                selectedSettingState = selectedSettingState,
                onSwitch = onOverlaySwitch,
                onToggleSetting = onToggleSetting
            )

            // USB デバッグ
            SettingCard(
                title =  stringResource(id = R.string.title_setting_usb_debug),
                onOff =  usbDebugState.key,
                onSwitch = { onUsbDebugSwitch() }
            )

            // インターネット接続
            InternetSettingCard(
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp)
                    .clickable { goAppSetting() },
                horizontalAlignment = Alignment.End
            ) {
                Image(
                    modifier = Modifier.clickable { goAppSetting() },
                    painter = painterResource(id = R.drawable.outline_build_40),
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
fun HomeContentPreview() {
    HomeContent(
        internetStateFlow = MutableStateFlow(InternetStateType.OFF),
        overlayStateFlow = MutableStateFlow(OverlayStateType.OFF),
        usbDebugStateFlow = MutableStateFlow(UsbDebugStateType.OFF),
        selectedSettingStateFlow = MutableStateFlow(SelectedOverlayType.USB_DEBUG),
        onInternetSwitch = {},
        onOverlaySwitch = {},
        onUsbDebugSwitch = {},
        onToggleSetting = {},
        goTutorial = {},
        goAppSetting = {}
    )
}
