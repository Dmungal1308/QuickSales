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
import com.google.gson.Gson
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.databinding.ActivityUsuariosBinding
import com.iesvdc.acceso.quicksales.ui.adapter.UsersAdapter
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.UsersViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.LogoutConfirmationDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra la lista de usuarios registrados (sólo accesible para administradores).
 *
 * Funcionalidades:
 * 1. Presenta los usuarios en un RecyclerView con GridLayout de 2 columnas.
 * 2. Al hacer clic en un usuario, abre [UserDetailActivity] con los detalles de dicho usuario.
 * 3. Incluye un DrawerLayout para navegar a otras pantallas (Mis Productos, Favoritos, Cartera, etc.).
 * 4. Permite cerrar sesión mediante un diálogo de confirmación.
 * 5. Muestra/oculta la opción "Usuarios" en el menú lateral según el rol del usuario (admin).
 * 6. Carga y muestra el avatar del usuario actual en el Drawer y en el botón superior, si existe.
 */
@AndroidEntryPoint
class UsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsuariosBinding
    private lateinit var drawerLayout: DrawerLayout

    /** ViewModel que maneja la lista de usuarios y el evento de logout. */
    private val vm: UsersViewModel by viewModels()

    /** ViewModel que proporciona información del perfil del usuario (avatar, rol). */
    private val settingsVm: SettingsViewModel by viewModels()

    /**
     * Se ejecuta al crear la Activity:
     * 1. Infla el layout mediante ViewBinding.
     * 2. Ajusta la barra de estado en blanco con texto oscuro si la API lo permite.
     * 3. Ajusta padding para no superponer barras del sistema.
     * 4. Configura RecyclerView con GridLayoutManager de 2 columnas.
     * 5. Observa LiveData de vm.users para poblar el adaptador con [UsersAdapter].
     * 6. Maneja evento de logout para redirigir a LoginActivity.
     * 7. Configura listeners de navegación en el Drawer (Mis Productos, Favoritos, Cartera, etc.).
     * 8. Muestra opción "Usuarios" sólo si el rol es "admin".
     * 9. Observa settingsVm.profile para cargar avatar del usuario en el Drawer y en el botón.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsuariosBinding.inflate(layoutInflater)
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
        vm.users.observe(this) { list ->
            binding.recyclerView.adapter = list?.let {
                UsersAdapter(it) { user ->
                    startActivity(
                        Intent(this, UserDetailActivity::class.java)
                            .putExtra("user", Gson().toJson(user))
                    )
                }
            }
        }

        // Observar evento de logout para redirigir a LoginActivity
        vm.logoutEvent.observe(this) {
            if (it) {
                vm.resetLogoutEvent()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        // Configurar botón para abrir/cerrar Drawer
        binding.imageButton.setOnClickListener { toggleDrawer() }
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener { toggleDrawer() }

        // Opción de cerrar sesión: muestra diálogo de confirmación
        findViewById<TextView>(R.id.cerrarSesion).setOnClickListener { showLogoutConfirmationDialog() }

        // Navegación a otras pantallas desde el Drawer
        binding.root.findViewById<TextView>(R.id.mis_productos)
            .setOnClickListener {
                startActivity(Intent(this, MyProductsActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        binding.root.findViewById<TextView>(R.id.favoritos)
            .setOnClickListener {
                startActivity(Intent(this, FavoritesActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        findViewById<ImageButton>(R.id.btnFavoritos)
            .setOnClickListener {
                startActivity(Intent(this, FavoritesActivity::class.java))
            }
        binding.root.findViewById<TextView>(R.id.cartera)
            .setOnClickListener {
                startActivity(Intent(this, WalletActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        binding.root.findViewById<TextView>(R.id.textView3)
            .setOnClickListener {
                startActivity(Intent(this, MenuActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        binding.root.findViewById<TextView>(R.id.productosVendidos)
            .setOnClickListener {
                startActivity(Intent(this, SoldProductsActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        findViewById<ImageButton>(R.id.btnChat)
            .setOnClickListener {
                startActivity(Intent(this, ChatRecopiladosActivity::class.java))
            }
        binding.root.findViewById<TextView>(R.id.productosComprados)
            .setOnClickListener {
                startActivity(Intent(this, PurchasedProductsActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        findViewById<ImageButton>(R.id.btnInicio)
            .setOnClickListener {
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }

        // Mostrar opción "Usuarios" sólo si el rol es "admin"
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
     * Al reanudar la Activity, recarga la lista de usuarios.
     */
    override fun onResume() {
        super.onResume()
        vm.loadUsers()
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
                startActivity(Intent(this@UsersActivity, LoginActivity::class.java))
                finish()
            }
        }
        dialog.show(supportFragmentManager, "LogoutConfirmationDialog")
    }
}
