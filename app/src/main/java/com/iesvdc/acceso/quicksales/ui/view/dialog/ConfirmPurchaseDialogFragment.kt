package com.iesvdc.acceso.quicksales.ui.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.iesvdc.acceso.quicksales.databinding.DialogConfirmPurchaseBinding
import com.iesvdc.acceso.quicksales.ui.modelview.FavoritesViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.MenuViewModel

/**
 * DialogFragment que muestra una confirmación para la compra de un producto.
 *
 * Recibe como argumentos:
 *  - ARG_ID: ID del producto a comprar.
 *  - ARG_NAME: Nombre del producto a mostrar en el mensaje.
 *  - ARG_PRICE: Precio del producto a mostrar en el mensaje.
 *  - ARG_FROM_FAV: Boolean que indica si la compra se origina desde la pantalla de favoritos.
 *
 * Al confirmar, se invoca la compra en el ViewModel correspondiente:
 *  - Si proviene de favoritos, se utiliza [FavoritesViewModel].
 *  - Si no, se utiliza [MenuViewModel].
 *
 * También observa los LiveData [MenuViewModel.purchaseSuccess] y [MenuViewModel.purchaseError]
 * para cerrar el diálogo en caso de éxito o mostrar un Toast en caso de error.
 */
class ConfirmPurchaseDialogFragment : DialogFragment() {

    /** Binding asociado al layout de confirmación de compra. */
    private var _binding: DialogConfirmPurchaseBinding? = null
    private val binding get() = _binding!!

    /** ViewModel principal que maneja la lógica de compra desde el menú. */
    private val vm: MenuViewModel by activityViewModels()

    /** ID del producto a comprar, obtenido de los argumentos. */
    private var productId: Int = 0

    /** Nombre del producto a mostrar en el mensaje de confirmación. */
    private var productName: String = ""

    /** Precio del producto a mostrar en el mensaje de confirmación. */
    private var productPrice: Double = 0.0

    /** Instancia de [MenuViewModel] para manejar compras (alias a vm). */
    private val menuVm: MenuViewModel        by activityViewModels()

    /** ViewModel alternativo que maneja compras cuando se proviene de favoritos. */
    private val favVm: FavoritesViewModel by activityViewModels()

    /** Indica si la compra se origina desde la pantalla de favoritos. */
    private var fromFav = false

    companion object {
        private const val ARG_ID    = "arg_id"
        private const val ARG_NAME  = "arg_name"
        private const val ARG_PRICE = "arg_price"
        private const val ARG_FROM_FAV = "from_fav"

        /**
         * Crea una nueva instancia de [ConfirmPurchaseDialogFragment] con los datos del producto.
         *
         * @param id ID del producto a comprar.
         * @param name Nombre del producto.
         * @param price Precio del producto.
         * @param fromFav True si la compra se origina desde la pantalla de favoritos.
         * @return Instancia configurada del diálogo.
         */
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

    /**
     * Crea y configura el diálogo de confirmación de compra.
     *
     * - Infla el layout usando [DialogConfirmPurchaseBinding].
     * - Recupera los argumentos (ID, nombre, precio y bandera fromFav).
     * - Muestra el mensaje de confirmación con nombre y precio formateado.
     * - Configura el botón "Cancelar" para descartar el diálogo.
     * - Configura el botón "Confirmar" para invocar la compra en el ViewModel
     *   adecuado ([MenuViewModel] o [FavoritesViewModel]) y descartar el diálogo.
     * - Observa los LiveData [MenuViewModel.purchaseSuccess] y [MenuViewModel.purchaseError]
     *   para cerrar el diálogo en caso de éxito o mostrar un Toast en caso de error.
     *
     * @param savedInstanceState Bundle de estado guardado (si existe).
     * @return Instancia de [Dialog] con la vista configurada.
     */
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

    /**
     * Libera la referencia al binding cuando la vista se destruye para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
