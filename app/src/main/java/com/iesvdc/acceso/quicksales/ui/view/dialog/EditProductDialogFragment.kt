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
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.DialogAddProductBinding
import com.iesvdc.acceso.quicksales.ui.modelview.MyProductsViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.io.ByteArrayOutputStream
import java.math.BigDecimal

/**
 * DialogFragment que permite editar los datos de un producto existente.
 *
 * Muestra un formulario con los campos:
 *  - Nombre del producto
 *  - Descripción
 *  - Precio
 *  - Imagen (previsualizada)
 *
 * Al guardar, recopila los valores editados, crea un nuevo objeto [ProductResponse]
 * con los datos actualizados y llama a [MyProductsViewModel.updateProduct] para persistir
 * los cambios. También permite seleccionar una nueva imagen desde la galería, convertirla
 * a Base64 y mostrarla en la previsualización.
 *
 * @param product Objeto [ProductResponse] que contiene los datos actuales del producto a editar.
 */
@AndroidEntryPoint
class EditProductDialogFragment(
    private val product: ProductResponse
) : DialogFragment() {

    /** Binding para acceder a los elementos de la vista del diálogo. */
    private var _binding: DialogAddProductBinding? = null
    private val binding get() = _binding!!

    /** ViewModel compartido que gestiona las operaciones de productos propios. */
    private val vm: MyProductsViewModel by activityViewModels()

    /**
     * Cadena Base64 de la imagen seleccionada o existente.
     * Inicializada con la imagen actual del producto (si existe).
     */
    private var imageBase64: String? = product.imagenBase64

    /**
     * Se llama al crear el diálogo. Infla la vista usando [DialogAddProductBinding],
     * rellena los campos con los datos del [product] actual, configura los listeners
     * para seleccionar imagen, cancelar y guardar los cambios.
     *
     * - Al hacer clic en la previsualización de imagen, llama a [pickImage] para abrir la galería.
     * - Al presionar "Cancelar", cierra el diálogo sin guardar.
     * - Al presionar "Guardar":
     *     1. Obtiene los valores de nombre, descripción y precio de los EditText.
     *     2. Convierte el precio a [BigDecimal], usando cero si no es válido.
     *     3. Crea un nuevo [ProductResponse] con los datos actualizados.
     *     4. Llama a [MyProductsViewModel.updateProduct] con el ID del producto y el nuevo objeto.
     *     5. Cierra el diálogo.
     *
     * @param savedInstanceState Bundle con el estado previo del fragmento (si existe).
     * @return Instancia de [Dialog] configurada.
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddProductBinding.inflate(layoutInflater)

        // Rellenar campos con los datos del producto dado
        binding.editName.setText(product.nombre)
        binding.editDescription.setText(product.descripcion)
        binding.editPrice.setText(product.precio.toPlainString())

        // Si el producto ya tiene imagen, decodificarla y mostrar previsualización
        product.imagenBase64?.let {
            val bytes = Base64.decode(it, Base64.DEFAULT)
            Glide.with(this).asBitmap().load(bytes).into(binding.imagePreview)
        }

        // Configurar listener para seleccionar imagen desde la galería
        binding.imagePreview.setOnClickListener { pickImage() }

        // Botón "Cancelar": cierra el diálogo
        binding.buttonCancel.setOnClickListener { dismiss() }

        // Botón "Guardar": recopila valores y actualiza el producto
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

    /**
     * Inicia una intención para seleccionar una imagen de la galería.
     * Al recibir el resultado en [onActivityResult], se convertirá a Base64.
     */
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1002)
    }

    /**
     * Maneja el resultado de la selección de imagen:
     *  - Verifica que el requestCode corresponda a 1002 y que la URI no sea nula.
     *  - Obtiene un [Bitmap] a partir de la URI seleccionada.
     *  - Convierte el [Bitmap] a Base64 mediante [encodeToBase64] y actualiza [imageBase64].
     *  - Muestra la imagen seleccionada en la previsualización usando Glide.
     *
     * @param requestCode Código de solicitud enviado en [pickImage] (1002).
     * @param resultCode  Código de resultado devuelto por la actividad de selección de imagen.
     * @param data        Intent que contiene la URI de la imagen seleccionada.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002 && data?.data != null) {
            val uri = data.data!!
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            imageBase64 = encodeToBase64(bitmap)
            Glide.with(this).load(bitmap).into(binding.imagePreview)
        }
    }

    /**
     * Convierte un [Bitmap] a una cadena Base64:
     *  - Crea un [ByteArrayOutputStream].
     *  - Comprime el [Bitmap] en formato JPEG con calidad al 80%.
     *  - Convierte el array de bytes resultante a Base64 y lo retorna.
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
     * Libera la referencia al binding cuando la vista se destruye, para evitar fugas de memoria.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
