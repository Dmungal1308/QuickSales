package com.iesvdc.acceso.quicksales.ui.view

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.appcompat.app.AlertDialog
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.ui.modelview.MisProductosViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteProductDialogFragment(
    private val productId: Int
) : DialogFragment() {

    private val vm: MisProductosViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater
            .inflate(R.layout.dialog_delete_product, null)

        val btnCancel = view.findViewById<TextView>(R.id.button_cancel)
        val btnConfirm = view.findViewById<TextView>(R.id.button_confirm)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnConfirm.setOnClickListener {
            vm.deleteProduct(productId)
            dialog.dismiss()
        }

        return dialog
    }
}

