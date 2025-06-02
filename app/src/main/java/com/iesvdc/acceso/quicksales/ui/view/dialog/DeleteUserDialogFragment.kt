package com.iesvdc.acceso.quicksales.ui.view.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.iesvdc.acceso.quicksales.R

/**
 * DialogFragment que muestra un diálogo para confirmar la eliminación de un usuario.
 *
 * @param onConfirm Lambda que se ejecuta cuando el usuario confirma la acción de eliminación.
 *                   Se invoca antes de cerrar el diálogo.
 */
class DeleteUserDialogFragment(
    private val onConfirm: () -> Unit
) : DialogFragment() {

    /**
     * Se llama al crear el diálogo. Infla el layout `dialog_delete_user`, configura
     * los listeners para los botones de cancelar y confirmar, y construye el AlertDialog.
     *
     * - El botón de cancelar simplemente cierra el diálogo sin más acción.
     * - El botón de confirmar invoca la función `onConfirm` pasada en el constructor,
     *   y luego cierra el diálogo.
     *
     * @param savedInstanceState Bundle con el estado previo del fragmento (si existe).
     * @return Instancia de [Dialog] para mostrar al usuario.
     */
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
