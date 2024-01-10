package dev.seabat.android.usbdebugswitch.compose.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.R

@Composable
fun SettingsItem(
    painter: Painter,
    text: String,
    optionalText: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column {
        Divider(modifier = Modifier.fillMaxWidth(), color = Color(0xFf7a5832))
        Row(
            modifier = if (onClick != null) {
                Modifier
                    .padding()
                    .clickable { onClick() }
                    .padding(vertical = 16.dp)
            } else {
                Modifier
                    .padding()
                    .padding(vertical = 16.dp)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier.clickable { },
                painter = painter,
                contentDescription = "privacy_policy",
            )
            Text(
                modifier = Modifier.weight(1.0f).padding(start = 8.dp),
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
            if (optionalText != null) {
                Text(
                    text = optionalText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview
@Composable
fun SettingsItemPreview() {
    SettingsItem(
        painter = painterResource(id = R.drawable.baseline_lock_outline_30),
        text = stringResource(id = R.string.settings_privacy_policy),
        onClick = {}
    )
}