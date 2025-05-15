package com.iesvdc.acceso.quicksales.ui.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.domain.usercase.LogoutUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LogoutConfirmationDialogFragment : DialogFragment() {

    @Inject lateinit var logoutUseCase: LogoutUseCase
    var onLogoutConfirmed: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater
            .inflate(R.layout.dialog_logout, null)

        // Referencias a botones
        val btnCancel = view.findViewById<TextView>(R.id.button_cancel)
        val btnConfirm = view.findViewById<TextView>(R.id.button_confirm)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        // Click “No”
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        // Click “Sí”
        btnConfirm.setOnClickListener {
            logoutUseCase()
            onLogoutConfirmed?.invoke()
            dialog.dismiss()
        }

        return dialog
    }
}
