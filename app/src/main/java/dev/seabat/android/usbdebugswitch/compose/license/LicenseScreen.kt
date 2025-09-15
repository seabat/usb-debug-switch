package dev.seabat.android.usbdebugswitch.compose.license

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.seabat.android.usbdebugswitch.R
import dev.seabat.android.usbdebugswitch.compose.LoadingComponent
import dev.seabat.android.usbdebugswitch.utils.LibraryLicenseList
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    licensesStateFlow: StateFlow<LibraryLicenseList>,
    licenseLoadedStateFlow: StateFlow<Boolean>,
    goBack: () -> Unit
) {
    val licenseLoaded by licenseLoadedStateFlow.collectAsState()
    if (licenseLoaded) {
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
                                text = stringResource(id = R.string.license_title),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    },
                    navigationIcon = {
                        Image(
                            modifier = Modifier.clickable { goBack() },
                            painter = painterResource(id = R.drawable.baseline_chevron_left_40),
                            contentDescription = "back",
                        )
                    }
                )
            }
        ) { contentPadding ->
            Box {
                // 背景用の Composable 関数
                Column(
                    modifier = Modifier.fillMaxSize().background(color = Color(0xFFF2F0F4))
                ) {}

                // contentPadding を考慮した本体コンテンツ
                LicenseContent(
                    modifier = Modifier
                        .padding(contentPadding)
                        .fillMaxSize()
                        .background(color = Color(0xFFF2F0F4))
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    licensesStateFlow = licensesStateFlow,
                )
            }
        }
    } else {
        LoadingComponent()
    }
}