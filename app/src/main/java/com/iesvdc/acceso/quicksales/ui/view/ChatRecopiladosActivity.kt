package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.databinding.ActivityChatRecopiladosBinding
import com.iesvdc.acceso.quicksales.ui.adapter.ChatSessionsAdapter
import com.iesvdc.acceso.quicksales.ui.modelview.ChatRecopiladosViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.LogoutConfirmationDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra las sesiones de chat del usuario, agrupadas por producto.
 *
 * Funcionalidad principal:
 * 1. Configura DrawerLayout y ajusta márgenes según barras del sistema.
 * 2. Configura RecyclerView con [ChatSessionsAdapter] para listar sesiones.
 * 3. Observa LiveData de [ChatRecopiladosViewModel] para cargar y actualizar sesiones.
 * 4. Maneja navegación dentro del menú lateral: productos, favoritos, cartera, etc.
 * 5. Permite logout mostrando [LogoutConfirmationDialogFragment].
 */
@AndroidEntryPoint
class ChatRecopiladosActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var binding: ActivityChatRecopiladosBinding
    private val vm: ChatRecopiladosViewModel by viewModels()
    private lateinit var adapter: ChatSessionsAdapter

    private val settingsVm: SettingsViewModel by viewModels()

    /**
     * Se ejecuta al crear la Activity:
     * - Infla el layout, configura DrawerLayout y padding por barras del sistema.
     * - Inicializa RecyclerView en modo LinearLayout (versión Grid comentada).
     * - Observa LiveData de sesiones (ChatRecopiladosViewModel.items) para poblar el adaptador.
     * - Carga las sesiones llamando a vm.load().
     * - Asigna listeners a botones de menú lateral y navegación entre pantallas.
     * - Ajusta visibilidad de la opción "Usuarios" según rol de usuario (admin).
     * - Observa perfil del usuario (SettingsViewModel.profile) para mostrar avatar en el Drawer.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRecopiladosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout

        // Barra de estado en blanco con texto oscuro si la API lo soporta
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Ajustar padding para barras del sistema (status/navigation)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Inicialmente configuramos RecyclerView con GridLayout de 2 columnas (puede cambiarse)
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Observa evento de logout para redirigir a LoginActivity
        vm.logoutEvent.observe(this) {
            if (it) {
                vm.resetLogoutEvent()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        // Configurar RecyclerView como lista lineal y asignar adaptador
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatSessionsAdapter(emptyList()) { item ->
            // Al hacer clic en una sesión, abre ChatActivity con los parámetros necesarios
            startActivity(Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_SESSION_ID,    item.session.idSesion)
                putExtra(ChatActivity.EXTRA_PRODUCT_JSON,  Gson().toJson(item.product))
                putExtra(ChatActivity.EXTRA_VENDEDOR_ID,   item.session.idVendedor)
                putExtra(ChatActivity.EXTRA_COMPRADOR_ID,  item.session.idComprador)
            })
        }
        binding.recyclerView.adapter = adapter

        // Observar la lista de elementos (sesiones + producto) y actualizar el adaptador
        vm.items.observe(this) { list ->
            adapter.updateList(list)
        }
        // Cargar las sesiones una vez
        vm.load()

        // Configurar botones y opciones del menú lateral
        binding.imageButton.setOnClickListener { toggleDrawer() }
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener { toggleDrawer() }
        findViewById<TextView>(R.id.cerrarSesion).setOnClickListener { showLogoutConfirmationDialog() }

        // Navegación a otras pantallas desde el Drawer
        binding.root.findViewById<TextView>(R.id.mis_productos)
            .setOnClickListener {
                startActivity(Intent(this, MyProductsActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        binding.root.findViewById<TextView>(R.id.favoritos).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<ImageButton>(R.id.btnFavoritos).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
        binding.root.findViewById<TextView>(R.id.cartera).setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.root.findViewById<TextView>(R.id.textView3).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.root.findViewById<TextView>(R.id.productosVendidos).setOnClickListener {
            startActivity(Intent(this, SoldProductsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.root.findViewById<TextView>(R.id.productosComprados).setOnClickListener {
            startActivity(Intent(this, PurchasedProductsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Mostrar opción de "Usuarios" solo si el rol del usuario es admin
        val tvUsuarios = binding.root.findViewById<TextView>(R.id.usuarios)
        settingsVm.profile.observe(this) { me ->
            tvUsuarios.visibility = if (me?.rol == "admin") View.VISIBLE else View.GONE
        }
        tvUsuarios.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Botón home que regresa a MenuActivity y cierra esta Activity
        findViewById<ImageButton>(R.id.btnInicio).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        // Observar perfil para cargar avatar en el botón de usuario y en el Drawer
        val navUserButton = binding.root.findViewById<ImageButton>(R.id.botonUsuario)
        settingsVm.profile.observe(this) { user ->
            val imageBytes = user?.imagenBase64?.let { Base64.decode(it, Base64.DEFAULT) }
            if (imageBytes != null) {
                val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(this).load(bmp).circleCrop().into(binding.imageButton3)
                Glide.with(this).load(bmp).circleCrop().into(navUserButton)
            } else {
                binding.imageButton3.setImageResource(R.mipmap.ic_logo_principal_foreground)
                navUserButton.setImageResource(R.mipmap.ic_logo_principal_foreground)
            }
        }
        navUserButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    /**
     * Abre o cierra el drawer lateral según su estado actual.
     */
    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            drawerLayout.openDrawer(GravityCompat.START)
    }

    /**
     * Muestra un diálogo de confirmación de logout.
     * Si se confirma, ejecuta logout y redirige a LoginActivity.
     */
    private fun showLogoutConfirmationDialog() {
        val dialog = LogoutConfirmationDialogFragment().apply {
            onLogoutConfirmed = {
                startActivity(Intent(this@ChatRecopiladosActivity, LoginActivity::class.java))
                finish()
            }
        }
        dialog.show(supportFragmentManager, "LogoutConfirmationDialog")
    }
}
