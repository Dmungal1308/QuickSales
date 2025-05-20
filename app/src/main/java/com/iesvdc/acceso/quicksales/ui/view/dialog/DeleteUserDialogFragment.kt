package com.iesvdc.acceso.quicksales.ui.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.iesvdc.acceso.quicksales.R

class DeleteUserDialogFragment(
    private val onConfirm: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_delete_user, null)

        root.findViewById<TextView>(R.id.button_cancel).setOnClickListener {
            dismiss()
        }
        root.findViewById<TextView>(R.id.button_confirm).setOnClickListener {
            onConfirm()
            dismiss()
        }

        return androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setView(root)
            .create()
    }
}
