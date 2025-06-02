package com.iesvdc.acceso.quicksales.ui.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.appcompat.app.AlertDialog
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.ui.modelview.MyProductsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * DialogFragment que muestra una confirmación para eliminar un producto.
 *
 * Muestra un layout con dos botones ("Cancelar" y "Confirmar"). Al confirmar,
 * se invoca la función [MyProductsViewModel.deleteProduct] con el ID proporcionado
 * y se cierra el diálogo.
 *
 * @param productId Identificador del producto que se desea eliminar.
 */
@AndroidEntryPoint
class DeleteProductDialogFragment(
    private val productId: Int
) : DialogFragment() {

    /** ViewModel compartido que maneja la lógica de productos propios del usuario. */
    private val vm: MyProductsViewModel by activityViewModels()

    /**
     * Crea y configura el diálogo de confirmación de eliminación.
     *
     * - Infla el layout `R.layout.dialog_delete_product`.
     * - Recupera las referencias a los botones de "Cancelar" y "Confirmar" mediante sus IDs.
     * - Configura el botón "Cancelar" para simplemente descartar el diálogo sin acción.
     * - Configura el botón "Confirmar" para:
     *   1. Llamar a [MyProductsViewModel.deleteProduct] con el `productId`.
     *   2. Cerrar el diálogo.
     *
     * @param savedInstanceState Bundle de estado guardado (si existe).
     * @return Instancia de [Dialog] con la vista configurada.
     */
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
