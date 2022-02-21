package usbdebugswitch.seabat.java_conf.gr.jp.usbdebugswitch

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface

import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.DialogFragment

class MessageDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = arguments?.getString(ARG_MESSAGE)

        return AlertDialog.Builder(activity)
            .setMessage(message)
            .setPositiveButton("OK",
                    object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            (activity as MainActivity).onClick()
                            dismiss()
                        }
                    }).create()
    }

    override fun onPause() {
        super.onPause()

        dismiss()
    }

    companion object {
        // constants

        val ARG_MESSAGE = "message"

        // methods

        fun newInstance(message: String): MessageDialogFragment {
            val instance = MessageDialogFragment()

            val arguments = Bundle()
            arguments.putString(ARG_MESSAGE, message)
            instance.arguments = arguments

            return instance
        }
    }

    interface OnClickListener {
        fun onClick();
    }
}
