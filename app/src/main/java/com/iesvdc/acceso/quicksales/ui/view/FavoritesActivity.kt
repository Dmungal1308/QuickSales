package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.databinding.ActivityFavoritosBinding
import com.iesvdc.acceso.quicksales.ui.adapter.ProductAdapter
import com.iesvdc.acceso.quicksales.ui.modelview.FavoritesViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.ConfirmPurchaseDialogFragment
import com.iesvdc.acceso.quicksales.ui.view.dialog.LogoutConfirmationDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra los productos marcados como favoritos por el usuario.
 *
 * Funcionalidades principales:
 * 1. Configura la vista con un RecyclerView en Grid de 2 columnas para listar productos.
 * 2. Permite filtrar la lista por nombre mediante un SearchView.
 * 3. Muestra un DrawerLayout con navegación a otras pantallas (Mis Productos, Cartera, etc.).
 * 4. Gestiona eventos de compra desde favoritos (mostrando ConfirmPurchaseDialogFragment).
 * 5. Observa LiveData de FavoritesViewModel para actualizar la lista de favoritos y sus IDs.
 * 6. Permite cerrar sesión mostrando LogoutConfirmationDialogFragment.
 */
@AndroidEntryPoint
class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private lateinit var drawerLayout: DrawerLayout
    private val vm: FavoritesViewModel by viewModels()
    private val settingsVm: SettingsViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    /**
     * Se ejecuta al crear la Activity:
     * - Infla el layout mediante ViewBinding.
     * - Configura barra de estado en blanco con texto oscuro si la API lo soporta.
     * - Ajusta padding para barras del sistema (status/navigation).
     * - Inicializa el ProductAdapter con callbacks para:
     *     * Compra: abre ConfirmPurchaseDialogFragment.
     *     * Toggle favorito: elimina de favoritos.
     *     * Clic en ítem: navega a ProductDetailActivity.
     * - Configura el RecyclerView en Grid de 2 columnas.
     * - Configura el SearchView para filtrar por nombre.
     * - Configura los listeners de menú lateral (toggleDrawer, navegación a otras pantallas).
     * - Observa LiveData de vm.favoriteIds para actualizar iconos de favorito.
     * - Observa LiveData de vm.products para actualizar la lista mostrada.
     * - Ajusta visibilidad de la opción “Usuarios” según rol de usuario (admin).
     * - Observa LiveData de vm.logoutEvent para navegar a LoginActivity al cerrar sesión.
     * - Observa LiveData de vm.purchaseError para mostrar Toast en caso de error al comprar.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout

        // Barra de estado en blanco con texto oscuro si API >= M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Ajustar padding para no superponer barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Inicializar adaptador con callbacks para comprar, toggle favorito y clic en ítem
        adapter = ProductAdapter(
            onBuy = { p ->
                ConfirmPurchaseDialogFragment
                    .newInstance(p.id, p.nombre, p.precio.toDouble(), true)
                    .show(supportFragmentManager, "confirm_purchase")
            },
            onToggleFavorite = { vm.removeFavorite(it) },
            onItemClick = { product ->
                startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                })
            }
        )

        // Configurar RecyclerView en Grid de 2 columnas
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        // Filtrado de productos por nombre usando SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also {
                vm.filterByName(query ?: "")
            }
            override fun onQueryTextChange(newText: String?) = true.also {
                vm.filterByName(newText ?: "")
            }
        })

        // Botones para abrir/cerrar drawer y navegar en el menú lateral
        binding.imageButton.setOnClickListener { toggleDrawer() }
        binding.root.findViewById<ImageButton>(R.id.botonFlecha)
            .setOnClickListener { toggleDrawer() }
        binding.root.findViewById<TextView>(R.id.cerrarSesion)
            .setOnClickListener { showLogoutConfirmationDialog() }
        binding.root.findViewById<TextView>(R.id.mis_productos)
            .setOnClickListener {
                startActivity(Intent(this, MyProductsActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        binding.root.findViewById<TextView>(R.id.cartera)
            .setOnClickListener {
                startActivity(Intent(this, WalletActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        findViewById<ImageButton>(R.id.btnInicio)
            .setOnClickListener {
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnChat).setOnClickListener {
            startActivity(Intent(this, ChatRecopiladosActivity::class.java))
        }
        binding.root.findViewById<TextView>(R.id.productosComprados).setOnClickListener {
            startActivity(Intent(this, SoldProductsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Observar IDs de favoritos para actualizar iconos en adaptador
        vm.favoriteIds.observe(this) { favSet ->
            adapter.setFavorites(favSet)
        }

        // Observar lista de productos favoritos y actualizar adaptador
        vm.products.observe(this) { lista ->
            adapter.submitList(lista) {
                adapter.setFavorites(vm.favoriteIds.value.orEmpty())
            }
        }

        // Mostrar opción de "Usuarios" solo si el rol es admin
        val tvUsuarios = binding.root.findViewById<TextView>(R.id.usuarios)
        settingsVm.profile.observe(this) { me ->
            tvUsuarios.visibility = if (me?.rol == "admin") View.VISIBLE else View.GONE
        }
        tvUsuarios.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Observa evento logout para redirigir a LoginActivity
        vm.logoutEvent.observe(this) {
            if (it) {
                vm.resetLogoutEvent()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        // Botón de usuario en el drawer navega a SettingsActivity
        binding.imageButton3.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Navegación a pantalla principal desde menú lateral
        binding.root.findViewById<TextView>(R.id.productosVendidos).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Mostrar avatar de usuario en botón de usuario y en drawer
        val navUserButton = binding.root.findViewById<ImageButton>(R.id.botonUsuario)
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

        // Mostrar error de compra en Toast y limpiar LiveData en ViewModel
        vm.purchaseError.observe(this) { err ->
            err?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                vm.clearPurchaseError()
            }
        }
    }

    /**
     * Al reanudar la Activity, recarga la lista de favoritos.
     */
    override fun onResume() {
        super.onResume()
        vm.loadFavorites()
    }

    /**
     * Abre o cierra el drawer lateral según su estado actual.
     */
    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
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
