package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.databinding.ActivityChatBinding
import com.iesvdc.acceso.quicksales.ui.adapter.ChatAdapter
import com.iesvdc.acceso.quicksales.ui.modelview.ChatViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.SellerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_SESSION_ID    = "extra_session_id"
        const val EXTRA_PRODUCT_JSON  = "extra_product_json"
        const val EXTRA_VENDEDOR_ID   = "extra_vendedor_id"
        const val EXTRA_COMPRADOR_ID  = "extra_comprador_id"
    }

    private lateinit var binding: ActivityChatBinding
    private val vm: ChatViewModel by viewModels()
    private val sellerVm: SellerViewModel by viewModels()
    private var userIdActual: Int = -1
    private var vendedorId: Int = -1
    private var compradorId: Int = -1
    private lateinit var product: ProductResponse
    private var pollingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        userIdActual =
            getSharedPreferences("SessionPrefs", MODE_PRIVATE)
                .getInt("user_id", -1)

        val sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)
        val prodJson  = intent.getStringExtra(EXTRA_PRODUCT_JSON)
        vendedorId    = intent.getIntExtra(EXTRA_VENDEDOR_ID, -1)
        compradorId   = intent.getIntExtra(EXTRA_COMPRADOR_ID, -1)

        if (sessionId < 0 || prodJson.isNullOrEmpty()) {
            Toast.makeText(this, "Datos de sesión incompletos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        product = Gson().fromJson(prodJson, ProductResponse::class.java)
        binding.tvProductInfo.text = "${product.nombre} — € %.2f".format(product.precio)

        val partnerId = if (userIdActual == vendedorId) compradorId else vendedorId
        sellerVm.loadUser(partnerId)
        sellerVm.user.observe(this) { user ->
            binding.tvPartnerName.text = user.nombreUsuario
            user.imagenBase64?.let { b64 ->
                Base64.decode(b64, Base64.DEFAULT)
                    .let { bytes -> BitmapFactory.decodeByteArray(bytes,0,bytes.size) }
                    .also { bmp ->
                        Glide.with(this).load(bmp).circleCrop()
                            .into(binding.imgPartner)
                    }
            }
        }

        binding.headerContainer.setOnClickListener {
            if (userIdActual == compradorId) {
                startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                })
            }
        }

        val adapter = ChatAdapter(emptyList(), userIdActual)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter        = adapter

        binding.rvChat.setSaveEnabled(false)

        vm.mensajes.observe(this) { msgs ->
            adapter.updateList(msgs)
            binding.rvChat.scrollToPosition(msgs.size - 1)
        }
        vm.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        binding.botonFlecha.setOnClickListener { finish() }

        binding.btnEnviar.setOnClickListener {
            binding.etMensaje.text.toString().trim()
                .takeIf { it.isNotEmpty() }
                ?.also {
                    vm.enviarMensaje(it)
                    binding.etMensaje.setText("")
                }
        }

        vm.iniciarSesionSessionId(sessionId)
    }
    override fun onStart() {
        super.onStart()
        pollingJob = lifecycleScope.launch {
            while(isActive) {
                vm.cargarMensajes()
                delay(5_000)
            }
        }
    }
    override fun onStop() {
        pollingJob?.cancel()
        super.onStop()
    }
    override fun onSaveInstanceState(outState: Bundle) {
    }
}
