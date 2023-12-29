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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    overlayStateFlow: StateFlow<String>,
    usbDebugStateFlow: StateFlow<String>,
    onOverlaySwitch: () -> Unit,
    onUsbDebugSwitch: () -> Unit
) {
    val overlayState by overlayStateFlow.collectAsState()
    val usbDebugState by usbDebugStateFlow.collectAsState()

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
                    .size(100.dp)
                    .clip(CircleShape),
                painter = painterResource(id = R.mipmap.ic_launcher_foreground_toumei),
                contentDescription = stringResource(id = R.string.app_name)
            )

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
                        text = stringResource(id = R.string.title_setting_overlay),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Switch(
                        checked = overlayState == "ON",
                        onCheckedChange = { onOverlaySwitch() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFFFFF),
                            checkedTrackColor = Color(0xFF75565c)
                        )
                    )
                }
            }

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
                Row(modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(100.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1.0f),
                        text = stringResource(id = R.string.title_setting_usb_debug),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Switch(
                        checked = usbDebugState == "ON",
                        onCheckedChange = { onUsbDebugSwitch() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFFFFF),
                            checkedTrackColor = Color(0xFF75565c)
                        )
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun MainScreenPreview() {
    MainScreen(
        MutableStateFlow("ON"),
        MutableStateFlow(""),
        onOverlaySwitch = {},
        onUsbDebugSwitch = {}
    )
}