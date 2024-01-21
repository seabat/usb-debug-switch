package dev.seabat.android.usbdebugswitch.compose.home

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.seabat.android.usbdebugswitch.R

@Composable
fun SwitchOverlayStateWarningDialog(onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onClose() },
        confirmButton = {
            TextButton(
                onClick = { onClose() }
            ) {
                Text(stringResource(id = R.string.overlay_state_warning_dialog_close))
            }
        },
        dismissButton = {},
        text = {
            Text(text = stringResource(R.string.overlay_state_switch_warning))
        }
    )
}