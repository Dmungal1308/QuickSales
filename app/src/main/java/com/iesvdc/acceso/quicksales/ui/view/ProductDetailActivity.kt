package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ActivityProductDetailBinding
import com.iesvdc.acceso.quicksales.ui.modelview.FavoritosViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.MenuViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.SellerViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.ConfirmPurchaseDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUCT = "extra_product"
    }

    private lateinit var binding: ActivityProductDetailBinding
    private val menuVm: MenuViewModel by viewModels()
    private val favVm: FavoritosViewModel by viewModels()
    private val sellerVm: SellerViewModel by viewModels() // Use case para GET /users/{id}
    private var isFav = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }


        // 1) Obtener el producto pasado en el Intent
        val product = intent
            .getStringExtra(EXTRA_PRODUCT)
            ?.let { Gson().fromJson(it, ProductResponse::class.java) }
            ?: return finish()

        // 2) Pintar datos
        binding.tvName.text        = product.nombre
        binding.tvDescription.text = product.descripcion
        binding.tvPrice.text       = "€ %.2f".format(product.precio)
        product.imagenBase64?.let { b64 ->
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            Glide.with(this).load(bytes).into(binding.imgProduct)
        }

        menuVm.favoriteIdsLive.observe(this) { favSet ->
            isFav = favSet.contains(product.id)
            updateFavIcon()
        }
        // inicialmente fuera de observe aún no haya valor; forzamos cargar:
        menuVm.loadData()

        // 4) Al click togglear:
        binding.btnFav.setOnClickListener {
            menuVm.toggleFavorite(product)
        }

        // 3) Cargar info de vendedor
        sellerVm.loadUser(product.idVendedor)
        sellerVm.user.observe(this) { user ->
            binding.tvSellerUsername.text = user.nombreUsuario
            user.imagenBase64
                ?.let { Base64.decode(it, Base64.DEFAULT) }
                ?.let { BitmapFactory.decodeByteArray(it,0,it.size) }
                ?.let { Glide.with(this).load(it).circleCrop().into(binding.imgSeller) }
        }

        // 4) Botones
        binding.btnBuy.setOnClickListener {
            ConfirmPurchaseDialogFragment
                .newInstance(product.id, product.nombre, product.precio.toDouble(), fromFav=false)
                .show(supportFragmentManager, "confirm_purchase")
        }
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }

        // 5) Observa error de compra
        menuVm.purchaseError.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                menuVm.clearPurchaseError()
            }
        }
    }
    private fun updateFavIcon() {
        binding.btnFav.setImageResource(
            if (isFav) R.mipmap.ic_corazon_lleno_foreground
            else       R.mipmap.ic_corazon_vacio_foreground
        )
    }
}
