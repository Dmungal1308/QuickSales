package com.iesvdc.acceso.quicksales.ui.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.domain.usercase.login.LogoutUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * DialogFragment que muestra un diálogo de confirmación para cerrar la sesión del usuario.
 *
 * Muestra dos botones:
 *  - "Cancelar": cierra el diálogo sin realizar ninguna acción.
 *  - "Confirmar": ejecuta el caso de uso [LogoutUseCase], invoca el callback
 *    [onLogoutConfirmed], y cierra el diálogo.
 *
 * Se puede pasar un listener a [onLogoutConfirmed] para realizar acciones adicionales
 * una vez que el usuario confirme el logout (por ejemplo, navegar a la pantalla de login).
 */
@AndroidEntryPoint
class LogoutConfirmationDialogFragment : DialogFragment() {

    /** Caso de uso para cerrar la sesión del usuario. Inyectado por Hilt. */
    @Inject lateinit var logoutUseCase: LogoutUseCase

    /**
     * Lambda opcional que se ejecuta después de confirmar el logout.
     * Puede asignarse desde la actividad o fragmento que instancia este diálogo
     * para ejecutar lógica adicional tras el cierre de sesión (ej. navegación).
     */
    var onLogoutConfirmed: (() -> Unit)? = null

    /**
     * Crea y configura el diálogo de confirmación de logout.
     *
     * - Infla el layout `R.layout.dialog_logout`.
     * - Obtiene referencias a los botones "Cancelar" y "Confirmar" mediante sus IDs.
     * - Configura el botón "Cancelar" para descartar el diálogo sin acción.
     * - Configura el botón "Confirmar" para:
     *     1. Ejecutar [logoutUseCase] para cerrar la sesión del usuario.
     *     2. Invocar [onLogoutConfirmed], si no es nula.
     *     3. Cerrar el diálogo.
     *
     * @param savedInstanceState Bundle con el estado previo del fragmento (si existe).
     * @return Instancia de [Dialog] configurada.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater
            .inflate(R.layout.dialog_logout, null)

        val btnCancel = view.findViewById<TextView>(R.id.button_cancel)
        val btnConfirm = view.findViewById<TextView>(R.id.button_confirm)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        btnConfirm.setOnClickListener {
            // Ejecutar el caso de uso para cerrar sesión
            logoutUseCase()
            // Invocar callback si está asignado
            onLogoutConfirmed?.invoke()
            dialog.dismiss()
        }

        return dialog
    }
}
