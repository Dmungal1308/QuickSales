package com.iesvdc.acceso.quicksales.ui.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.iesvdc.acceso.quicksales.databinding.DialogConfirmPurchaseBinding
import com.iesvdc.acceso.quicksales.ui.modelview.FavoritosViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.MenuViewModel

class ConfirmPurchaseDialogFragment : DialogFragment() {

    private var _binding: DialogConfirmPurchaseBinding? = null
    private val binding get() = _binding!!
    private val vm: MenuViewModel by activityViewModels()

    private var productId: Int = 0
    private var productName: String = ""
    private var productPrice: Double = 0.0
    private val menuVm: MenuViewModel        by activityViewModels()
    private val favVm: FavoritosViewModel by activityViewModels()
    private var fromFav = false

    companion object {
        private const val ARG_ID    = "arg_id"
        private const val ARG_NAME  = "arg_name"
        private const val ARG_PRICE = "arg_price"
        private const val ARG_FROM_FAV = "from_fav"
        fun newInstance(id: Int, name: String, price: Double, fromFav: Boolean = false) =
            ConfirmPurchaseDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_ID, id)
                    putString(ARG_NAME, name)
                    putDouble(ARG_PRICE, price)
                    putBoolean(ARG_FROM_FAV, fromFav)
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
        vm.purchaseSuccess.observe(this) { success ->
            if (success) {
                dismiss()
                vm.clearPurchaseSuccess()
            }
        }
        vm.purchaseError.observe(this) { err ->
            err?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                vm.clearPurchaseError()
            }
        }
        fromFav = arguments?.getBoolean(ARG_FROM_FAV, false) ?: false

        binding.buttonConfirm.setOnClickListener {
            if (fromFav) favVm.purchaseById(productId)
            else         menuVm.purchaseById(productId)
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
