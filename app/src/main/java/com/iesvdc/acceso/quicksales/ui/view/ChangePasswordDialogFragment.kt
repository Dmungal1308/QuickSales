package com.iesvdc.acceso.quicksales.ui.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.iesvdc.acceso.quicksales.databinding.DialogChangePasswordBinding
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordDialogFragment : DialogFragment() {

    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val vm: SettingsViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogChangePasswordBinding.inflate(layoutInflater)

        binding.btnCancelPwd.setOnClickListener { dismiss() }
        binding.btnConfirmPwd.setOnClickListener {
            val p1 = binding.editNewPassword.text.toString()
            val p2 = binding.editConfirmPassword.text.toString()
            if (p1.isNotEmpty() && p1 == p2) {
                vm.changePassword(p1)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            }
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
