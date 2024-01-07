package dev.seabat.android.usbdebugswitch.compose.home

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingCard(title: String, onOff: String, onSwitch: (String) -> Unit) {
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
            modifier = Modifier
                .padding(start = 20.dp, end = 20.dp)
                .height(80.dp),
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
fun SettingCardPreview() {
    SettingCard(title = "タイトル", onOff = "on", onSwitch = {})
}
