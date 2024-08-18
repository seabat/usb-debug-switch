package dev.seabat.android.usbdebugswitch.compose.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.constants.OverlayStateType
import dev.seabat.android.usbdebugswitch.constants.SelectedOverlayType

@Composable
fun OverlaySettingCard(
    title: String = stringResource(id = R.string.title_setting_overlay),
    overlayState: OverlayStateType,
    selectedSettingState: SelectedOverlayType,
    onOverlaySettingWarning: () -> Unit,
    onSwitch: () -> Unit,
    onToggleSetting: (SelectedOverlayType) -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFBEFF6)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
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
                    Pair(
                        stringResource(id = R.string.title_setting_usb_debug),
                        painterResource(id = R.mipmap.ic_on)
                    ),
                    Pair(
                        stringResource(id = R.string.title_setting_internet),
                        painterResource(id = R.mipmap.ic_online)
                    )
                )

                radioOptions.forEach { pair ->
                    Row(
                        modifier = Modifier.height(30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF75565c)),
                            selected = pair.first == when (selectedSettingState) {
                                SelectedOverlayType.USB_DEBUG -> radioOptions[0].first
                                SelectedOverlayType.INTERNET -> radioOptions[1].first
                            },
                            onClick = {
                                if (overlayState.isOn()) {
                                    onToggleSetting(
                                        when (pair.first) {
                                            radioOptions[0].first -> SelectedOverlayType.USB_DEBUG
                                            radioOptions[1].first -> SelectedOverlayType.INTERNET
                                            else -> SelectedOverlayType.USB_DEBUG
                                        }
                                    )
                                } else {
                                    onOverlaySettingWarning()
                                }
                            }
                        )
                        Text(
                            text = pair.first,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Image(
                            modifier = Modifier.padding(start = 8.dp).size(17.dp).alpha(0.85f),
                            painter = pair.second,
                            contentDescription = "privacy_policy",
                        )
                    }
                }
            }

            Switch(
                checked = overlayState == OverlayStateType.ON,
                onCheckedChange = { onSwitch() },
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
fun OverlaySettingCardPreview() {
    OverlaySettingCard(
        title = "画面オーバーレイ",
        overlayState = OverlayStateType.ON,
        selectedSettingState = SelectedOverlayType.USB_DEBUG,
        onOverlaySettingWarning = {},
        onSwitch = {},
        onToggleSetting = {}
    )
}