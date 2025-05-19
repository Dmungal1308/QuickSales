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

@AndroidEntryPoint
class EditProfileDialogFragment : DialogFragment() {

    private var _binding: DialogEditProfileBinding? = null
    private val binding get() = _binding!!
    private val vm: SettingsViewModel by activityViewModels()
    private var avatarBase64: String? = null

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2001 && data?.data != null) {
            val uri = data.data!!
            val bmp = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            avatarBase64 = encodeToBase64(bmp)
            Glide.with(this).load(bmp).into(binding.imageAvatar)
        }
    }

    private fun encodeToBase64(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
