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
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.DialogAddProductBinding
import com.iesvdc.acceso.quicksales.ui.modelview.MisProductosViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.math.BigDecimal

@AndroidEntryPoint
class AddProductDialogFragment : DialogFragment() {

    private var _binding: DialogAddProductBinding? = null
    private val binding get() = _binding!!
    private val vm: MisProductosViewModel by activityViewModels()
    private var imageBase64: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddProductBinding.inflate(layoutInflater)
        binding.imagePreview.setOnClickListener { pickImage() }

        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener {
            val nombre = binding.editName.text.toString()
            val descripcion = binding.editDescription.text.toString()
            val precio = binding.editPrice.text.toString()
                .toBigDecimalOrNull() ?: BigDecimal.ZERO
            vm.createProduct(
                ProductResponse(
                    id = 0,
                    nombre = nombre,
                    descripcion = descripcion,
                    imagenBase64 = imageBase64,
                    precio = precio,
                    estado = "activo",
                    idVendedor = 0,
                    idComprador = null
                )
            )
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun pickImage() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && data?.data != null) {
            val uri = data.data!!
            val bitmap = MediaStore.Images.Media
                .getBitmap(requireContext().contentResolver, uri)
            imageBase64 = encodeToBase64(bitmap)
            Glide.with(this).load(bitmap).into(binding.imagePreview)
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
