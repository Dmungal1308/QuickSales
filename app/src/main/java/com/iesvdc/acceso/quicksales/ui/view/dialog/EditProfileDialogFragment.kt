package com.iesvdc.acceso.quicksales.ui.view.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.databinding.DialogEditProfileBinding
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream

/**
 * DialogFragment que muestra un formulario para editar el perfil de usuario.
 *
 * - Al abrirse, recupera los datos actuales del usuario desde [SettingsViewModel.profile] y
 *   los muestra en los campos de nombre completo, nombre de usuario y correo.
 * - Si el usuario ya tiene un avatar (imagen en Base64), lo decodifica y lo muestra en [binding.imageAvatar].
 * - Permite seleccionar una nueva imagen de avatar desde la galería; al seleccionarla, la convierte
 *   a Base64 mediante [encodeToBase64] y la muestra como previsualización.
 * - Al presionar "Guardar", recopila los valores de los campos y llama a [SettingsViewModel.updateProfile]
 *   con los datos ingresados (nombre completo, nombre de usuario, correo e imagen en Base64).
 * - Al presionar "Cancelar", simplemente cierra el diálogo sin guardar cambios.
 */
@AndroidEntryPoint
class EditProfileDialogFragment : DialogFragment() {

    /** Binding para acceder a los elementos de la vista del diálogo. */
    private var _binding: DialogEditProfileBinding? = null
    private val binding get() = _binding!!

    /** ViewModel compartido que maneja la lógica de perfil y actualización. */
    private val vm: SettingsViewModel by activityViewModels()

    /** Cadena Base64 del avatar seleccionado o existente; inicializado en null. */
    private var avatarBase64: String? = null

    /**
     * Se llama al crear el diálogo. Infla la vista usando [DialogEditProfileBinding], rellena
     * los campos con los datos actuales del usuario y configura los listeners para seleccionar avatar,
     * cancelar y guardar.
     *
     * - Si [vm.profile.value] no es nulo, obtiene el nombre, nombre de usuario, correo y avatar,
     *   y los asigna a los campos correspondientes.
     * - Al hacer clic en la imagen de avatar, abre la galería mediante una Intent ACTION_PICK
     *   y espera el resultado en [onActivityResult] con el código 2001.
     * - El botón "Cancelar" cierra el diálogo sin guardar cambios.
     * - El botón "Guardar" llama a [vm.updateProfile] con los valores de los campos y la imagen en Base64,
     *   luego cierra el diálogo.
     *
     * @param savedInstanceState Bundle con el estado previo del fragmento (si existe).
     * @return Instancia de [Dialog] configurada.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogEditProfileBinding.inflate(layoutInflater)

        vm.profile.value?.let { user ->
            binding.editFullName.setText(user.nombre)
            binding.editUsername.setText(user.nombreUsuario)
            binding.editEmail.setText(user.correo)
            avatarBase64 = user.imagenBase64
            user.imagenBase64?.let {
                val bytes = Base64.decode(it, Base64.DEFAULT)
                Glide.with(this).asBitmap().load(bytes).into(binding.imageAvatar)
            }
        }

        binding.imageAvatar.setOnClickListener {
            val pick = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pick, 2001)
        }

        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener {
            vm.updateProfile(
                nombre     = binding.editFullName.text.toString(),
                nombreUsuario     = binding.editUsername.text.toString(),
                correo        = binding.editEmail.text.toString(),
                imagenBase64 = avatarBase64
            )
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    /**
     * Maneja el resultado de la selección de imagen para el avatar:
     *
     * - Verifica que [requestCode] sea 2001 y que [data]?.data no sea nulo.
     * - Obtiene un [Bitmap] a partir de la URI seleccionada.
     * - Convierte el [Bitmap] a Base64 llamando a [encodeToBase64], y asigna la cadena a [avatarBase64].
     * - Muestra la imagen seleccionada en [binding.imageAvatar] usando Glide.
     *
     * @param requestCode Código de solicitud enviado en [pickImage] (2001).
     * @param resultCode  Código de resultado devuelto por la actividad de selección de imagen.
     * @param data        Intent que contiene la URI de la imagen seleccionada.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2001 && data?.data != null) {
            val uri = data.data!!
            val bmp = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            avatarBase64 = encodeToBase64(bmp)
            Glide.with(this).load(bmp).into(binding.imageAvatar)
        }
    }

    /**
     * Convierte un [Bitmap] a una cadena Base64 comprimida en formato JPEG al 80% de calidad.
     *
     * - Crea un [ByteArrayOutputStream], comprime el [Bitmap] al 80% de calidad JPEG
     *   y obtiene los bytes resultantes.
     * - Codifica los bytes resultantes a Base64 y retorna la cadena.
     *
     * @param bm [Bitmap] a convertir.
     * @return Cadena Base64 que representa la imagen comprimida.
     */
    private fun encodeToBase64(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    /**
     * Libera la referencia a [DialogEditProfileBinding] cuando la vista se destruye,
     * para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
