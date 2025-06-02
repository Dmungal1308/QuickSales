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

/**
 * Activity que muestra la conversación de chat entre comprador y vendedor.
 *
 * Recibe por Intent:
 *  - EXTRA_SESSION_ID: ID de la sesión de chat.
 *  - EXTRA_PRODUCT_JSON: JSON del producto que se está negociando.
 *  - EXTRA_VENDEDOR_ID y EXTRA_COMPRADOR_ID: IDs de participante.
 *
 * Funcionalidad principal:
 * 1. Configura la UI (barra de estado, vista de datos de producto y partner).
 * 2. Inicializa RecyclerView con ChatAdapter para mostrar mensajes.
 * 3. Observa LiveData de ChatViewModel para actualizar lista de mensajes y errores.
 * 4. Realiza polling cada 5 segundos (en onStart) para recargar mensajes.
 * 5. Envía nuevos mensajes y permite ir a detalle de producto al pulsar cabecera.
 */
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

    /**
     * Método de ciclo de vida called cuando se crea la Activity.
     * - Infla layout y configura vistas estáticas (barra de estado, SharedPreferences).
     * - Recupera parámetros del Intent y valida que sean válidos.
     * - Muestra datos del producto (nombre y precio).
     * - Carga y observa info del partner en la conversación (nombre de usuario y avatar).
     * - Inicializa RecyclerView con ChatAdapter y configura observadores de mensajes/errores.
     * - Configura listeners para botón de atrás, envío de mensajes y detalle de producto.
     * - Inicia la sesión de chat llamando a ChatViewModel.iniciarSesionSessionId.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Barra de estado en blanco con texto oscuro si la API lo soporta
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Obtener ID de usuario actual desde SharedPreferences
        userIdActual =
            getSharedPreferences("SessionPrefs", MODE_PRIVATE)
                .getInt("user_id", -1)

        // Leer extras del Intent
        val sessionId = intent.getIntExtra(EXTRA_SESSION_ID, -1)
        val prodJson  = intent.getStringExtra(EXTRA_PRODUCT_JSON)
        vendedorId    = intent.getIntExtra(EXTRA_VENDEDOR_ID, -1)
        compradorId   = intent.getIntExtra(EXTRA_COMPRADOR_ID, -1)

        // Validar que los datos de sesión y producto estén presentes
        if (sessionId < 0 || prodJson.isNullOrEmpty()) {
            Toast.makeText(this, "Datos de sesión incompletos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Parsear JSON del producto y mostrar nombre/precio en UI
        product = Gson().fromJson(prodJson, ProductResponse::class.java)
        binding.tvProductInfo.text = "${product.nombre} — € %.2f".format(product.precio)

        // Determinar el ID del partner (si soy vendedor, el partner es comprador, y viceversa)
        val partnerId = if (userIdActual == vendedorId) compradorId else vendedorId

        // Cargar datos del partner y observar LiveData para mostrar nombre y avatar
        sellerVm.loadUser(partnerId)
        sellerVm.user.observe(this) { user ->
            binding.tvPartnerName.text = user.nombreUsuario
            user.imagenBase64?.let { b64 ->
                Base64.decode(b64, Base64.DEFAULT)
                    .let { bytes -> BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                    .also { bmp ->
                        Glide.with(this).load(bmp).circleCrop().into(binding.imgPartner)
                    }
            }
        }

        // Al hacer clic en la cabecera, si soy comprador, navegar a detalle de producto
        binding.headerContainer.setOnClickListener {
            if (userIdActual == compradorId) {
                startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                })
            }
        }

        // Configurar RecyclerView para la conversación usando ChatAdapter
        val adapter = ChatAdapter(emptyList(), userIdActual)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter        = adapter
        binding.rvChat.setSaveEnabled(false) // Desactivar guardado de estado para evitar saltos

        // Observar LiveData de mensajes para actualizar adaptador y hacer scroll al último mensaje
        vm.mensajes.observe(this) { msgs ->
            adapter.updateList(msgs)
            binding.rvChat.scrollToPosition(msgs.size - 1)
        }
        // Observar LiveData de error para mostrar Toast si ocurre un problema
        vm.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        // Botón de flecha atrás finaliza la Activity
        binding.botonFlecha.setOnClickListener { finish() }

        // Botón de enviar captura texto, lo envía al ViewModel y limpia el campo
        binding.btnEnviar.setOnClickListener {
            binding.etMensaje.text.toString().trim()
                .takeIf { it.isNotEmpty() }
                ?.also {
                    vm.enviarMensaje(it)
                    binding.etMensaje.setText("")
                }
        }

        // Iniciar o recuperar la sesión de chat con el ID proporcionado
        vm.iniciarSesionSessionId(sessionId)
    }

    /**
     * Se ejecuta cuando la Activity entra en primer plano.
     * Inicia un Job de polling que cada 5 segundos solicita la recarga de mensajes.
     */
    override fun onStart() {
        super.onStart()
        pollingJob = lifecycleScope.launch {
            while (isActive) {
                vm.cargarMensajes()
                delay(5_000)
            }
        }
    }

    /**
     * Se ejecuta cuando la Activity sale de primer plano.
     * Cancela el Job de polling para detener las actualizaciones periódicas de mensajes.
     */
    override fun onStop() {
        pollingJob?.cancel()
        super.onStop()
    }

    /**
     * Deshabilitado para evitar guardar el estado de la vista, ya que no es necesario
     * y podría interferir con el comportamiento del chat.
     */
    override fun onSaveInstanceState(outState: Bundle) {
    }
}
