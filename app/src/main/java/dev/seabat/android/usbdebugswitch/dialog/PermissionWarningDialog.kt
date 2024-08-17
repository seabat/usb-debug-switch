package dev.seabat.android.usbdebugswitch.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class PermissionWarningDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = arguments?.getString(ARG_MESSAGE) ?: ""
        val requestCode = arguments?.getString(ARG_REQUEST_CODE) ?: ""

        return AlertDialog.Builder(activity)
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                parentFragmentManager.setFragmentResult(
                    requestCode,
                    Bundle()
                )
                dismiss()
            }.create()
    }

    override fun onPause() {
        super.onPause()

        dismiss()
    }

    companion object {
        // constants

        const val ARG_MESSAGE = "message"
        const val ARG_REQUEST_CODE = "request_code"

        // methods

        fun newInstance(message: String, requestCode: String): PermissionWarningDialog =
            PermissionWarningDialog().also { dialog ->
                dialog.arguments = Bundle().also { bundle ->
                    bundle.putString(ARG_MESSAGE, message)
                    bundle.putString(ARG_REQUEST_CODE, requestCode)
                }
            }
    }
}
