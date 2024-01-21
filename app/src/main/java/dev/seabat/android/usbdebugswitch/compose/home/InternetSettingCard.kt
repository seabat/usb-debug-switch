package dev.seabat.android.usbdebugswitch.compose.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.R

@Composable
fun InternetSettingCard(
    title: String = stringResource(id = R.string.title_setting_internet),
    onInternetSettingWarning: () -> Unit,
    onOff: String,
    onSwitch: (String) -> Unit
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
                Column(modifier = Modifier.clickable { onInternetSettingWarning() }) {
                    Row(
                        modifier = Modifier.height(30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val mobileDataState = remember { mutableStateOf(false) }
                        Checkbox(
                            checked = mobileDataState.value,
                            enabled = false,
                            onCheckedChange = { mobileDataState.value = it }
                        )
                        Text(
                            text = stringResource(id = R.string.setting_mobile_data_connection),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.height(30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val wifiState = remember { mutableStateOf(true) }
                        Checkbox(
                            checked = wifiState.value,
                            enabled = false,
                            onCheckedChange = { wifiState.value = it }
                        )
                        Text(
                            text = stringResource(id = R.string.setting_wifi_connection),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

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
fun InternetSettingCardPreview() {
    InternetSettingCard(onOff = "on", onInternetSettingWarning = {}, onSwitch = {})
}
