package dev.seabat.android.usbdebugswitch.compose.license

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.seabat.android.usbdebugswitch.utils.LibraryLicense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun LicenseContent(
    modifier: Modifier = Modifier,
    licensesStateFlow: StateFlow<List<LibraryLicense>>
) {
    val licensesState by licensesStateFlow.collectAsState()
    LazyColumn(modifier = modifier) {
        items(licensesState) {
            Column() {
                Text(text = it.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it.terms, fontSize = 8.sp)
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview
@Composable
fun LicenseContentPreview() {
    LicenseContent(
        licensesStateFlow = MutableStateFlow<List<LibraryLicense>>(arrayListOf())
    )
}