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
import com.iesvdc.acceso.quicksales.ui.modelview.MyProductsViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.math.BigDecimal

/**
 * DialogFragment que muestra un formulario para agregar un nuevo producto.
 *
 * Permite al usuario:
 *  - Ingresar nombre, descripción y precio del producto.
 *  - Seleccionar una imagen desde la galería, convertirla a Base64 y mostrarla como previsualización.
 *  - Al presionar "Guardar", crea una instancia de [ProductResponse] con los datos ingresados
 *    y llama a [MyProductsViewModel.createProduct] para persistir el nuevo producto.
 *  - Al presionar "Cancelar", simplemente cierra el diálogo.
 *
 * Utiliza ViewModel compartido [MyProductsViewModel] para ejecutar la creación del producto.
 */
@AndroidEntryPoint
class AddProductDialogFragment : DialogFragment() {

    private var _binding: DialogAddProductBinding? = null
    private val binding get() = _binding!!
    private val vm: MyProductsViewModel by activityViewModels()
    private var imageBase64: String? = null

    /**
     * Crea y configura el diálogo con el layout de formulario de producto.
     *
     * - Infla la vista usando [DialogAddProductBinding].
     * - Configura el listener de clic en la previsualización de imagen para invocar [pickImage].
     * - Configura el botón "Cancelar" para cerrar el diálogo sin acción.
     * - Configura el botón "Guardar" para:
     *   1. Obtener los valores de nombre, descripción y precio de los EditText.
     *   2. Convertir el precio a [BigDecimal], tomando 0 si no se ingresó nada válido.
     *   3. Construir un objeto [ProductResponse] con los datos recopilados y la imagen en Base64 (si existe).
     *   4. Llamar a [MyProductsViewModel.createProduct] para persistir el producto.
     *   5. Cerrar el diálogo.
     *
     * @param savedInstanceState Bundle de estado guardado (si aplica).
     * @return Instancia de [Dialog] con la vista configurada.
     */
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

    /**
     * Abre la galería de imágenes mediante una [Intent] ACTION_PICK para que el usuario seleccione una imagen.
     * El resultado se maneja en [onActivityResult] con código 1001.
     */
    private fun pickImage() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(intent, 1001)
    }

    /**
     * Maneja el resultado de la selección de imagen de la galería.
     *
     * - Verifica que el código de solicitud sea 1001 y que el intent no sea nulo.
     * - Obtiene el URI de la imagen seleccionada y la convierte a [Bitmap] usando ContentResolver.
     * - Llama a [encodeToBase64] para convertir el [Bitmap] a cadena Base64 y la almacena en [imageBase64].
     * - Muestra la imagen seleccionada en el [binding.imagePreview] usando Glide.
     *
     * @param requestCode Código de solicitud enviado en [pickImage] (1001).
     * @param resultCode  Código de resultado devuelto por la actividad de selección de imagen.
     * @param data        Intent que contiene el URI de la imagen seleccionada.
     */
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

    /**
     * Convierte un [Bitmap] a cadena Base64 con compresión JPEG al 80% de calidad.
     *
     * - Crea un [ByteArrayOutputStream].
     * - Comprime el [Bitmap] en formato JPEG con calidad 80 y escribe los bytes en el stream.
     * - Codifica los bytes resultantes a Base64 usando [Base64] y retorna la cadena.
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
     * Libera la referencia a [DialogAddProductBinding] cuando la vista se destruye
     * para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
