package com.iesvdc.acceso.quicksales.ui.view.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.iesvdc.acceso.quicksales.databinding.DialogChangePasswordBinding
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * DialogFragment que muestra un formulario para cambiar la contraseña del usuario.
 *
 * - Permite al usuario ingresar y confirmar una nueva contraseña.
 * - Verifica que ambos campos coincidan antes de invocar [SettingsViewModel.changePassword].
 * - Muestra un Toast de error si las contraseñas no coinciden.
 */
@AndroidEntryPoint
class ChangePasswordDialogFragment : DialogFragment() {

    /** Binding asociado al layout del diálogo de cambio de contraseña. */
    private var _binding: DialogChangePasswordBinding? = null
    private val binding get() = _binding!!

    /** ViewModel compartido que maneja la lógica de perfil y cambio de contraseña. */
    private val vm: SettingsViewModel by activityViewModels()

    /**
     * Crea y configura el diálogo con el layout de cambio de contraseña.
     *
     * - Infla la vista usando [DialogChangePasswordBinding].
     * - Configura el botón "Cancelar" para cerrar el diálogo sin acción.
     * - Configura el botón "Confirmar" para:
     *   1. Obtener las contraseñas ingresadas en [binding.editNewPassword] y [binding.editConfirmPassword].
     *   2. Verificar que no estén vacías y que coincidan.
     *   3. Si coinciden, invocar [SettingsViewModel.changePassword] con la nueva contraseña y cerrar el diálogo.
     *   4. Si no coinciden, mostrar un Toast con el mensaje "Las contraseñas no coinciden".
     *
     * @param savedInstanceState Bundle de estado guardado (si aplica).
     * @return Instancia de [Dialog] con la vista configurada.
     */
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

    /**
     * Libera la referencia a [DialogChangePasswordBinding] cuando la vista se destruye
     * para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
