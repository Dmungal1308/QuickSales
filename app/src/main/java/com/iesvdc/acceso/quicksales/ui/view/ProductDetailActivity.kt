package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ActivityProductDetailBinding
import com.iesvdc.acceso.quicksales.ui.modelview.ChatViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.MenuViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.SellerViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.ConfirmPurchaseDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra los detalles de un producto específico.
 *
 * Funcionalidades:
 * - Muestra nombre, descripción, precio e imagen del producto.
 * - Permite marcar/desmarcar como favorito.
 * - Inicia la compra mostrando ConfirmPurchaseDialogFragment.
 * - Carga información del vendedor y muestra su nombre e imagen.
 * - Permite iniciar un chat con el vendedor.
 */
@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    companion object {
        /** Clave para recibir el ID de producto desde el Intent. */
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    private lateinit var binding: ActivityProductDetailBinding
    private val menuVm: MenuViewModel by viewModels()
    private val sellerVm: SellerViewModel by viewModels()
    private val chatVm: ChatViewModel by viewModels()

    private var isFav = false
    private var currentProduct: ProductResponse? = null
    private var compradorId: Int = -1

    /**
     * Se ejecuta al crear la Activity:
     * 1. Configura la barra de estado.
     * 2. Recupera el ID del comprador desde SharedPreferences.
     * 3. Obtiene el ID de producto del Intent y valida.
     * 4. Observa lista de productos en MenuViewModel para encontrar el producto solicitado.
     * 5. Muestra producto llamando a bindProduct cuando se encuentra.
     * 6. Observa purchaseSuccess y purchaseError para manejar resultados de compra.
     * 7. Configura el botón de retroceso.
     * 8. Observa ChatViewModel para iniciar ChatActivity cuando se reciba sesión.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar barra de estado en blanco con texto oscuro si API >= M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Obtener ID de comprador (usuario actual) desde SharedPreferences
        compradorId = getSharedPreferences("SessionPrefs", MODE_PRIVATE)
            .getInt("user_id", -1)

        // Obtener ID de producto del Intent
        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId < 0) return finish()

        // Observar lista de productos para encontrar el que coincide con productId
        menuVm.products.observe(this) { list ->
            list.find { it.id == productId }?.let { prod ->
                currentProduct = prod
                bindProduct(prod)
            }
        }
        // Cargar datos de productos en ViewModel
        menuVm.loadData()

        // Manejar resultado de compra: si es exitoso, volver al menú
        menuVm.purchaseSuccess.observe(this) { success ->
            if (success) {
                menuVm.clearPurchaseSuccess()
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
        }
        // Mostrar error de compra en Toast y limpiar LiveData
        menuVm.purchaseError.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                menuVm.clearPurchaseError()
            }
        }

        // Botón de retroceso cierra la Activity
        binding.botonFlecha.setOnClickListener { finish() }

        // Observar sesión de chat: al recibirla, abrir ChatActivity con datos necesarios
        chatVm.sesion.observe(this) { sesion ->
            val prod = currentProduct ?: return@observe
            Intent(this, ChatActivity::class.java).also {
                it.putExtra(ChatActivity.EXTRA_SESSION_ID, sesion.idSesion)
                it.putExtra(ChatActivity.EXTRA_PRODUCT_JSON, Gson().toJson(prod))
                it.putExtra(ChatActivity.EXTRA_VENDEDOR_ID, prod.idVendedor)
                it.putExtra(ChatActivity.EXTRA_COMPRADOR_ID, compradorId)
                startActivity(it)
            }
        }
        // Mostrar error de chat en Toast
        chatVm.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    /**
     * Vincula los datos del [product] a la UI:
     * - Muestra nombre, descripción, precio e imagen.
     * - Observa IDs de favoritos para actualizar el icono de favorito.
     * - Configura botón de favorito para alternar estado en MenuViewModel.
     * - Carga datos del vendedor y muestra su nombre de usuario e imagen.
     * - Configura botón de compra para mostrar ConfirmPurchaseDialogFragment.
     * - Configura botón de chat para iniciar sesión de chat.
     *
     * @param product Objeto [ProductResponse] con datos del producto a mostrar.
     */
    private fun bindProduct(product: ProductResponse) {
        binding.tvName.text = product.nombre
        binding.tvDescription.text = product.descripcion
        binding.tvPrice.text = "€ %.2f".format(product.precio)

        // Mostrar imagen del producto si está disponible
        product.imagenBase64?.let { b64 ->
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            Glide.with(this).load(bytes).into(binding.imgProduct)
        }

        // Observar IDs de favoritos para actualizar el icono
        menuVm.favoriteIdsLive.observe(this) { favSet ->
            isFav = favSet.contains(product.id)
            updateFavIcon()
        }
        // Botón de favorito alterna estado en ViewModel
        binding.btnFav.setOnClickListener {
            menuVm.toggleFavorite(product)
        }

        // Cargar datos del vendedor
        sellerVm.loadUser(product.idVendedor)
        sellerVm.user.observe(this) { user ->
            binding.tvSellerUsername.text = user.nombreUsuario
            user.imagenBase64
                ?.let { Base64.decode(it, Base64.DEFAULT) }
                ?.let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                ?.let { bmp ->
                    Glide.with(this).load(bmp).circleCrop().into(binding.imgSeller)
                }
        }

        // Botón de compra muestra diálogo de confirmación
        binding.btnBuy.setOnClickListener {
            ConfirmPurchaseDialogFragment
                .newInstance(product.id, product.nombre, product.precio.toDouble(), fromFav = false)
                .show(supportFragmentManager, "confirm_purchase")
        }

        // Botón de chat inicia sesión de chat con vendedor y comprador
        binding.btnChat.setOnClickListener {
            Log.d("ChatDebug", "Iniciando sesión: prod=${product.id}, vend=${product.idVendedor}, comp=$compradorId")
            chatVm.iniciarSesion(
                prodId = product.id,
                vendId = product.idVendedor,
                compId = compradorId
            )
        }
    }

    /**
     * Actualiza el icono del botón de favorito según el estado [isFav].
     */
    private fun updateFavIcon() {
        binding.btnFav.setImageResource(
            if (isFav) R.mipmap.ic_corazon_lleno_foreground
            else       R.mipmap.ic_corazon_vacio_foreground
        )
    }
}
