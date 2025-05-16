package com.iesvdc.acceso.quicksales.ui.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.iesvdc.acceso.quicksales.databinding.DialogConfirmPurchaseBinding
import com.iesvdc.acceso.quicksales.ui.modelview.MenuViewModel

class ConfirmPurchaseDialogFragment : DialogFragment() {

    private var _binding: DialogConfirmPurchaseBinding? = null
    private val binding get() = _binding!!
    private val vm: MenuViewModel by activityViewModels()

    private var productId: Int = 0
    private var productName: String = ""
    private var productPrice: Double = 0.0

    companion object {
        private const val ARG_ID    = "arg_id"
        private const val ARG_NAME  = "arg_name"
        private const val ARG_PRICE = "arg_price"
        fun newInstance(id: Int, name: String, price: Double) = ConfirmPurchaseDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ID, id)
                putString(ARG_NAME, name)
                putDouble(ARG_PRICE, price)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogConfirmPurchaseBinding.inflate(layoutInflater)

        arguments?.let {
            productId    = it.getInt(ARG_ID)
            productName  = it.getString(ARG_NAME, "")
            productPrice = it.getDouble(ARG_PRICE)
        }

        binding.textMessage.text =
            "¿Comprar \"$productName\" por € %.2f?".format(productPrice)

        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonConfirm.setOnClickListener {
            vm.purchaseById(productId)
            dismiss()
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
