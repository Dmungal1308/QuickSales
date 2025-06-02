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
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.databinding.ActivityMisProductosBinding
import com.iesvdc.acceso.quicksales.ui.adapter.MyProductsAdapter
import com.iesvdc.acceso.quicksales.ui.modelview.MyProductsViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.AddProductDialogFragment
import com.iesvdc.acceso.quicksales.ui.view.dialog.DeleteProductDialogFragment
import com.iesvdc.acceso.quicksales.ui.view.dialog.EditProductDialogFragment
import com.iesvdc.acceso.quicksales.ui.view.dialog.LogoutConfirmationDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra y gestiona los productos del usuario.
 *
 * Funcionalidades:
 * - Listar los productos propios del usuario en un RecyclerView.
 * - Filtrar productos por nombre usando un SearchView.
 * - Permitir agregar, editar y eliminar productos mediante diálogos.
 * - Incluir DrawerLayout con navegación a otras pantallas y cierre de sesión.
 */
@AndroidEntryPoint
class MyProductsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMisProductosBinding
    private lateinit var drawerLayout: DrawerLayout
    private val vm: MyProductsViewModel by viewModels()
    private val settingsVm: SettingsViewModel by viewModels()

    // Adapter que maneja edición y eliminación de productos
    private val adapter = MyProductsAdapter(
        onEdit   = { product ->
            // Mostrar diálogo para editar, pasando el producto seleccionado
            EditProductDialogFragment(product)
                .show(supportFragmentManager, "EditProductDialog")
        },
        onDelete = { product ->
            // Mostrar diálogo para confirmar eliminación, pasando el ID
            DeleteProductDialogFragment(product.id)
                .show(supportFragmentManager, "DeleteProductDialog")
        }
    )

    /**
     * Se ejecuta al crear la Activity:
     * 1. Infla el layout mediante ViewBinding.
     * 2. Configura la barra de estado en blanco con texto oscuro si la API lo permite.
     * 3. Ajusta padding del principal para no superponer barras del sistema.
     * 4. Inicializa RecyclerView con LinearLayoutManager y asigna el adapter.
     * 5. Configura FloatingActionButton para abrir AddProductDialogFragment.
     * 6. Configura SearchView para filtrar productos al escribir.
     * 7. Observa LiveData de vm.products para actualizar la lista.
     * 8. Configura listeners del DrawerLayout para navegación (Cartera, Menú, Favoritos, Chat, etc.).
     * 9. Observa perfil para mostrar/ocultar opción "Usuarios" y cargar avatar.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        // Barra de estado en blanco con texto oscuro (API >= M)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Ajustar padding para no superponer barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Configurar RecyclerView con LayoutManager y adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Acción del FloatingActionButton: abrir diálogo para agregar producto
        binding.fabAdd.setOnClickListener {
            AddProductDialogFragment().show(supportFragmentManager, "AddProductDialog")
        }

        // Filtrar lista al ingresar texto en SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                vm.filterByName(query ?: "")
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                vm.filterByName(newText ?: "")
                return true
            }
        })

        // Observar cambios en la lista de productos y actualizar adapter
        vm.products.observe(this) { list ->
            adapter.submitList(list)
        }

        // Configurar botón para abrir/cerrar Drawer
        binding.imageButton.setOnClickListener { toggleDrawer() }
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener { toggleDrawer() }

        // Opción de cerrar sesión: mostrar diálogo de confirmación
        findViewById<TextView>(R.id.cerrarSesion).setOnClickListener { showLogoutConfirmationDialog() }

        // Navegación a Cartera
        findViewById<TextView>(R.id.cartera).setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Navegación a Menú principal
        findViewById<TextView>(R.id.textView3).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Navegación a Favoritos
        binding.root.findViewById<TextView>(R.id.favoritos).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Navegación a Chat recopilado
        findViewById<ImageButton>(R.id.btnChat).setOnClickListener {
            startActivity(Intent(this, ChatRecopiladosActivity::class.java))
        }

        // Navegación a Favoritos (botón inferior)
        findViewById<ImageButton>(R.id.btnFavoritos).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        // Navegación a Ajustes de perfil
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Botón de inicio regresa al menú principal
        findViewById<ImageButton>(R.id.btnInicio).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        // Navegación a Comprados y Vendidos
        binding.root.findViewById<TextView>(R.id.productosComprados).setOnClickListener {
            startActivity(Intent(this, PurchasedProductsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.root.findViewById<TextView>(R.id.productosVendidos).setOnClickListener {
            startActivity(Intent(this, SoldProductsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Obtener referencia al botón de usuario en el Drawer
        val tvUsuarios = binding.root.findViewById<TextView>(R.id.usuarios)
        val navUserButton = binding.root.findViewById<ImageButton>(R.id.botonUsuario)

        // Mostrar opción "Usuarios" solo si el rol es admin
        settingsVm.profile.observe(this) { me ->
            tvUsuarios.visibility = if (me?.rol == "admin") View.VISIBLE else View.GONE
        }
        tvUsuarios.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Cargar avatar de usuario en el Drawer y en el botón principal
        settingsVm.profile.observe(this) { user ->
            val imageBytes = user?.imagenBase64
                ?.let { Base64.decode(it, Base64.DEFAULT) }

            if (imageBytes != null) {
                val bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(this)
                    .load(bmp)
                    .circleCrop()
                    .into(binding.imageButton3)

                Glide.with(this)
                    .load(bmp)
                    .circleCrop()
                    .into(navUserButton)
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
     * Abre o cierra el DrawerLayout según su estado actual.
     */
    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            drawerLayout.openDrawer(GravityCompat.START)
    }

    /**
     * Muestra un diálogo de confirmación de logout. Si se confirma,
     * navega a LoginActivity y cierra esta Activity.
     */
    private fun showLogoutConfirmationDialog() {
        val dialog = LogoutConfirmationDialogFragment()
        dialog.onLogoutConfirmed = {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        dialog.show(supportFragmentManager, "LogoutConfirmationDialog")
    }
}
