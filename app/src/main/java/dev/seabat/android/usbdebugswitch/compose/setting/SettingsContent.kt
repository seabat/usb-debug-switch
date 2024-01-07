package dev.seabat.android.usbdebugswitch.compose.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.seabat.android.usbdebugswitch.BuildConfig
import dev.seabat.android.usbdebugswitch.R

@Composable
fun SettingsContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SettingsItem(
            painterResource(id = R.drawable.baseline_info_outline_30),
            stringResource(id = R.string.settings_version),
            clickable = false,
            optionalText = "${BuildConfig.VERSION_NAME}"
        )
        SettingsItem(
            painterResource(id = R.drawable.baseline_lock_outline_30),
            stringResource(id = R.string.settings_privacy_policy),
            clickable = true
        )
        Divider(modifier = Modifier.fillMaxWidth(), color = Color(0xFf7a5832))
    }
}

@Preview
@Composable
fun SettingsContentPreview() {
    SettingsContent()
}