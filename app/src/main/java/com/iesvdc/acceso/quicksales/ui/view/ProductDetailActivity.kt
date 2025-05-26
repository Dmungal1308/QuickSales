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
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ActivityProductDetailBinding
import com.iesvdc.acceso.quicksales.ui.modelview.ChatViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.MenuViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.SellerViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.ConfirmPurchaseDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT_ID = "extra_product_id"
    }

    private lateinit var binding: ActivityProductDetailBinding
    private val menuVm: MenuViewModel by viewModels()
    private val sellerVm: SellerViewModel by viewModels()
    private val chatVm: ChatViewModel by viewModels()

    private var isFav = false
    private var currentProduct: ProductResponse? = null
    private var compradorId: Int = -1  // lo cargamos de prefs en onCreate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // recupera compradorId de SharedPrefs
        compradorId = getSharedPreferences("SessionPrefs", MODE_PRIVATE)
            .getInt("user_id", -1)

        val productId = intent.getIntExtra(EXTRA_PRODUCT_ID, -1)
        if (productId < 0) return finish()

        // Observa tu lista de productos y pinta solo el que toque
        menuVm.products.observe(this) { list ->
            list.find { it.id == productId }?.let { prod ->
                currentProduct = prod
                bindProduct(prod)
            }
        }
        menuVm.loadData()

        // Al confirmar compra, vuelves al menú
        menuVm.purchaseSuccess.observe(this) { success ->
            if (success) {
                menuVm.clearPurchaseSuccess()
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
        }
        menuVm.purchaseError.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                menuVm.clearPurchaseError()
            }
        }

        // Una sola flecha de “volver”
        binding.botonFlecha.setOnClickListener { finish() }

        // Cuando recibas la sesión de chat, navega a ChatActivity
        chatVm.sesion.observe(this) { sesion ->
            val i = Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_SESSION_ID, sesion.idSesion)
            }
            startActivity(i)
        }
        chatVm.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun bindProduct(product: ProductResponse) {
        // Datos básicos
        binding.tvName.text        = product.nombre
        binding.tvDescription.text = product.descripcion
        binding.tvPrice.text       = "€ %.2f".format(product.precio)
        product.imagenBase64?.let { b64 ->
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            Glide.with(this).load(bytes).into(binding.imgProduct)
        }

        // Favoritos
        menuVm.favoriteIdsLive.observe(this) { favSet ->
            isFav = favSet.contains(product.id)
            updateFavIcon()
        }
        binding.btnFav.setOnClickListener {
            menuVm.toggleFavorite(product)
        }

        // Vendedor
        sellerVm.loadUser(product.idVendedor)
        sellerVm.user.observe(this) { user ->
            binding.tvSellerUsername.text = user.nombreUsuario
            user.imagenBase64
                ?.let { Base64.decode(it, Base64.DEFAULT) }
                ?.let { BitmapFactory.decodeByteArray(it,0,it.size) }
                ?.let { Glide.with(this).load(it).circleCrop().into(binding.imgSeller) }
        }

        // Comprar
        binding.btnBuy.setOnClickListener {
            ConfirmPurchaseDialogFragment
                .newInstance(product.id, product.nombre, product.precio.toDouble(), fromFav = false)
                .show(supportFragmentManager, "confirm_purchase")
        }

        // ¡Aquí va el CHAT!
        binding.btnChat.setOnClickListener {
            Log.d("ChatDebug", "Iniciando sesión: prod=${product.id}, vend=${product.idVendedor}, comp=$compradorId")
            // Lanza la creación/obtención de la sesión
            chatVm.iniciarSesion(
                prodId = product.id,
                vendId = product.idVendedor,
                compId = compradorId
            )
        }
    }

    private fun updateFavIcon() {
        binding.btnFav.setImageResource(
            if (isFav) R.mipmap.ic_corazon_lleno_foreground
            else       R.mipmap.ic_corazon_vacio_foreground
        )
    }
}

