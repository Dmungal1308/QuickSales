package com.iesvdc.acceso.quicksales.ui.view

import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
    private val sellerVm: SellerViewModel by viewModels() // para cargar perfil de usuario
    private var userIdActual: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // 1) Leer datos del Intent
        userIdActual = getSharedPreferences("SessionPrefs", MODE_PRIVATE).getInt("user_id", -1)

        val sessionId   = intent.getIntExtra(EXTRA_SESSION_ID, -1)
        val prodJson    = intent.getStringExtra(EXTRA_PRODUCT_JSON)
        val vendedorId  = intent.getIntExtra(EXTRA_VENDEDOR_ID, -1)
        val compradorId = intent.getIntExtra(EXTRA_COMPRADOR_ID, -1)

        if (sessionId < 0 || prodJson == null) {
            Toast.makeText(this, "Datos de sesión incompletos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2) Cabecera de producto
        val product = Gson().fromJson(prodJson, ProductResponse::class.java)
        binding.tvProductInfo.text = "${product.nombre}  —  € %.2f".format(product.precio)

        // 3) Cabecera de usuario contrario
        val partnerId = if (userIdActual == vendedorId) compradorId else vendedorId
        sellerVm.loadUser(partnerId)
        sellerVm.user.observe(this) { user ->
            binding.tvPartnerName.text = user.nombreUsuario
            user.imagenBase64?.let { b64 ->
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                Glide.with(this)
                    .load(bytes)
                    .circleCrop()
                    .into(binding.imgPartner)
            }
        }

        // 4) RecyclerView de mensajes
        val adapter = ChatAdapter(emptyList(), userIdActual)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter        = adapter

        vm.mensajes.observe(this) { lista ->
            adapter.updateList(lista)
            binding.rvChat.scrollToPosition(lista.size - 1)
        }
        vm.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        // 5) Enviar
        binding.btnEnviar.setOnClickListener {
            val texto = binding.etMensaje.text.toString().trim()
            if (texto.isNotEmpty()) {
                vm.enviarMensaje(texto)
                binding.etMensaje.setText("")
            }
        }
        binding.botonFlecha.setOnClickListener { finish() }

        // 6) Cargar mensajes de la sesión
        vm.iniciarSesionSessionId(sessionId)
    }
}

