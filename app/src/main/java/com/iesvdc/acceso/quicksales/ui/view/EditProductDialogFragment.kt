// File: com/iesvdc/acceso/quicksales/ui/view/EditProductDialogFragment.kt
package com.iesvdc.acceso.quicksales.ui.view

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
import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.DialogAddProductBinding
import com.iesvdc.acceso.quicksales.ui.modelview.MisProductosViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.math.BigDecimal

@AndroidEntryPoint
class EditProductDialogFragment(
    private val product: ProductResponse
) : DialogFragment() {

    private var _binding: DialogAddProductBinding? = null
    private val binding get() = _binding!!
    private val vm: MisProductosViewModel by activityViewModels()
    private var imageBase64: String? = product.imagenBase64

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddProductBinding.inflate(layoutInflater)
        binding.editName.setText(product.nombre)
        binding.editDescription.setText(product.descripcion)
        binding.editPrice.setText(product.precio.toPlainString())
        product.imagenBase64?.let {
            val bytes = Base64.decode(it, Base64.DEFAULT)
            Glide.with(this).asBitmap().load(bytes).into(binding.imagePreview)
        }
        binding.imagePreview.setOnClickListener { pickImage() }
        binding.buttonCancel.setOnClickListener { dismiss() }
        binding.buttonSave.setOnClickListener {
            val nombre = binding.editName.text.toString()
            val descripcion = binding.editDescription.text.toString()
            val precio = binding.editPrice.text.toString().toBigDecimalOrNull() ?: BigDecimal.ZERO
            vm.updateProduct(
                product.id,
                ProductResponse(
                    id = product.id,
                    nombre = nombre,
                    descripcion = descripcion,
                    imagenBase64 = imageBase64,
                    precio = precio,
                    estado = product.estado,
                    idVendedor = product.idVendedor,
                    idComprador = product.idComprador
                )
            )
            dismiss()
        }
        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1002)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002 && data?.data != null) {
            val uri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
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
