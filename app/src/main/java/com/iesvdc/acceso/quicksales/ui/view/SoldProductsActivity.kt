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
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.databinding.ActivityMenuBinding
import com.iesvdc.acceso.quicksales.ui.adapter.PurchasedProductsAdapter
import com.iesvdc.acceso.quicksales.ui.modelview.SoldProductsViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.LogoutConfirmationDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra los productos vendidos por el usuario.
 *
 * - Presenta los productos vendidos en un RecyclerView con GridLayout de 2 columnas.
 * - Permite navegación a otras pantallas a través de un DrawerLayout.
 * - Muestra avatar del usuario si está disponible y oculta/ muestra la opción “Usuarios”
 *   según el rol.
 * - Gestiona el evento de logout mostrando un diálogo de confirmación.
 */
@AndroidEntryPoint
class SoldProductsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var drawerLayout: DrawerLayout

    /** ViewModel que proporciona la lista de productos vendidos y el evento de logout. */
    private val vm: SoldProductsViewModel by viewModels()

    /** ViewModel que maneja la información del perfil de usuario (avatar, rol). */
    private val settingsVm: SettingsViewModel by viewModels()

    /**
     * Configuración inicial de la Activity:
     * 1. Infla el layout con ViewBinding.
     * 2. Ajusta la barra de estado si la API lo permite.
     * 3. Aplica padding para no solapar barras del sistema.
     * 4. Inicializa RecyclerView con GridLayoutManager de 2 columnas.
     * 5. Observa LiveData de vm.products para mostrar productos vendidos.
     * 6. Observa vm.logoutEvent para redirigir a LoginActivity al cerrar sesión.
     * 7. Configura navegación en el Drawer hacia otras pantallas (Mis Productos, Favoritos, etc.).
     * 8. Observa settingsVm.profile para:
     *    - Mostrar/ocultar opción “Usuarios” si el rol es admin.
     *    - Cargar avatar del usuario en el botón superior y en el Drawer.
     * 9. Configura botón de inicio para regresar a MenuActivity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout

        // Barra de estado en blanco con texto oscuro si API >= M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Ajustar padding para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Configurar RecyclerView con GridLayoutManager de 2 columnas
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Observar lista de productos vendidos y asignar adapter
        vm.products.observe(this) { list ->
            binding.recyclerView.adapter = PurchasedProductsAdapter(list)
        }

        // Observar evento de logout para redirigir a LoginActivity
        vm.logoutEvent.observe(this) {
            if (it) {
                vm.resetLogoutEvent()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        // Configurar botones de menú lateral y navegación
        binding.imageButton.setOnClickListener { toggleDrawer() }
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener { toggleDrawer() }
        findViewById<TextView>(R.id.cerrarSesion).setOnClickListener { showLogoutConfirmationDialog() }
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
        findViewById<ImageButton>(R.id.btnChat).setOnClickListener {
            startActivity(Intent(this, ChatRecopiladosActivity::class.java))
        }
        binding.root.findViewById<TextView>(R.id.cartera).setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.root.findViewById<TextView>(R.id.textView3).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.root.findViewById<TextView>(R.id.productosComprados).setOnClickListener {
            startActivity(Intent(this, PurchasedProductsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<ImageButton>(R.id.btnInicio).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        // Mostrar opción “Usuarios” solo si el rol es admin
        val tvUsuarios = binding.root.findViewById<TextView>(R.id.usuarios)
        settingsVm.profile.observe(this) { me ->
            tvUsuarios.visibility = if (me?.rol == "admin") View.VISIBLE else View.GONE
        }
        tvUsuarios.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Cargar avatar del usuario en el Drawer y en el botón superior
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
     * Al reanudar la Activity, recarga la lista de productos vendidos.
     */
    override fun onResume() {
        super.onResume()
        vm.loadPurchased()
    }

    /**
     * Abre o cierra el DrawerLayout según su estado actual.
     */
    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            drawerLayout.openDrawer(GravityCompat.START)
    }

    /**
     * Muestra un diálogo de confirmación de logout. Si se confirma, navega a LoginActivity
     * y cierra esta Activity.
     */
    private fun showLogoutConfirmationDialog() {
        val dialog = LogoutConfirmationDialogFragment().apply {
            onLogoutConfirmed = {
                startActivity(Intent(this@SoldProductsActivity, LoginActivity::class.java))
                finish()
            }
        }
        dialog.show(supportFragmentManager, "LogoutConfirmationDialog")
    }
}
