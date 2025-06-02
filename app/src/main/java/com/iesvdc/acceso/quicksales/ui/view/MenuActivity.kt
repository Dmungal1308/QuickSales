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
import com.iesvdc.acceso.quicksales.databinding.ActivityMenuBinding
import com.iesvdc.acceso.quicksales.ui.adapter.ProductAdapter
import com.iesvdc.acceso.quicksales.ui.modelview.MenuViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.ConfirmPurchaseDialogFragment
import com.iesvdc.acceso.quicksales.ui.view.dialog.LogoutConfirmationDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity principal que muestra la lista de productos disponibles.
 *
 * Funcionalidades:
 * - Muestra productos en un GridLayout de 2 columnas.
 * - Permite búsqueda por nombre mediante SearchView.
 * - Gestiona favoritos (toggle) y compras desde la propia vista.
 * - Incluye un DrawerLayout con navegación a pantallas secundarias (Mis Productos, Favoritos, etc.).
 * - Muestra avatar de usuario en el botón de perfil y en el Drawer, según perfil cargado.
 * - Permite cerrar sesión mediante ConfirmLogoutDialogFragment.
 */
@AndroidEntryPoint
class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var drawerLayout: DrawerLayout

    private val vm: MenuViewModel by viewModels()
    private val settingsVm: SettingsViewModel by viewModels()

    private lateinit var adapter: ProductAdapter

    /**
     * Se ejecuta al crear la Activity:
     * 1. Infla el layout y configura la barra de estado (blanco con texto oscuro si API >= M).
     * 2. Ajusta padding del contenido para no superponer barras del sistema.
     * 3. Inicializa ProductAdapter con callbacks:
     *    - onBuy: muestra ConfirmPurchaseDialogFragment para confirmar la compra.
     *    - onToggleFavorite: alterna el estado de favorito desde ViewModel.
     *    - onItemClick: abre ProductDetailActivity para el producto seleccionado.
     * 4. Configura RecyclerView en Grid de 2 columnas con el adapter.
     * 5. Observa LiveData del ViewModel para:
     *    - products: lista de productos a mostrar.
     *    - favoriteIdsLive: IDs de favoritos para cambiar el icono.
     *    - logoutEvent: redirige a LoginActivity si se cierra sesión.
     * 6. Configura SearchView para filtrar productos por nombre en tiempo real.
     * 7. Configura listeners de íconos y TextView en el Drawer para navegación:
     *    - Mis Productos, Favoritos, Chat, Cartera, Productos Comprados/Vendidos, Usuarios (si admin).
     *    - Botones de usuario y ajustes de perfil para abrir SettingsActivity.
     * 8. Observa LiveData de perfil para cargar avatar:
     *    - Si el usuario tiene imagen en Base64, la decodifica y muestra.
     *    - Si no, muestra ícono por defecto.
     * 9. Observa purchaseError para mostrar Toast en caso de fallo al comprar.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout

        // Ajuste visual de la barra de estado (blanco con texto oscuro)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Aplicar padding al root para respetar barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Configurar ProductAdapter con callbacks de compra, favorito y clic en ítem
        adapter = ProductAdapter(
            onBuy = { product ->
                ConfirmPurchaseDialogFragment
                    .newInstance(product.id, product.nombre, product.precio.toDouble())
                    .show(supportFragmentManager, "confirm_purchase")
            },
            onToggleFavorite = { vm.toggleFavorite(it) },
            onItemClick = { product ->
                startActivity(Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.id)
                })
            }
        )

        // Configurar RecyclerView en Grid de 2 columnas
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        // Observar productos y actualizar lista cuando cambien
        vm.products.observe(this) { adapter.submitList(it) }
        // Observar IDs de favoritos para actualizar iconos en el adaptador
        vm.favoriteIdsLive.observe(this) { adapter.setFavorites(it) }
        // Manejar evento de logout: navegar a LoginActivity
        vm.logoutEvent.observe(this) {
            if (it) {
                vm.resetLogoutEvent()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        // Configurar SearchView para filtrar la lista de productos
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = true.also { vm.filterByName(q.orEmpty()) }
            override fun onQueryTextChange(t: String?) = true.also { vm.filterByName(t.orEmpty()) }
        })

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
        binding.root.findViewById<TextView>(R.id.productosComprados).setOnClickListener {
            startActivity(Intent(this, PurchasedProductsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.root.findViewById<TextView>(R.id.productosVendidos).setOnClickListener {
            startActivity(Intent(this, SoldProductsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Configurar botón de usuario en el Drawer para abrir SettingsActivity
        binding.imageButton3.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        val navUserButton = binding.root.findViewById<ImageButton>(R.id.botonUsuario)

        // Observar perfil para cargar avatar en botones
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

        // Mostrar opción "Usuarios" solo si el rol es admin
        val tvUsuarios = binding.root.findViewById<TextView>(R.id.usuarios)
        settingsVm.profile.observe(this) { me ->
            tvUsuarios.visibility = if (me?.rol == "admin") View.VISIBLE else View.GONE
        }
        tvUsuarios.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Observar error de compra y mostrar Toast, luego limpiar el error en ViewModel
        vm.purchaseError.observe(this) { errMsg ->
            errMsg?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                vm.clearPurchaseError()
            }
        }
    }

    /**
     * Al reanudar la Activity, recarga los datos de productos y favoritos.
     */
    override fun onResume() {
        super.onResume()
        vm.loadData()
    }

    /**
     * Abre o cierra el DrawerLayout según su estado actual.
     */
    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawer(GravityCompat.START)
        else drawerLayout.openDrawer(GravityCompat.START)
    }

    /**
     * Muestra un diálogo de confirmación de logout. Si se confirma, navega a LoginActivity
     * y cierra esta Activity.
     */
    private fun showLogoutConfirmationDialog() {
        val dialog = LogoutConfirmationDialogFragment().apply {
            onLogoutConfirmed = {
                startActivity(Intent(this@MenuActivity, LoginActivity::class.java))
                finish()
            }
        }
        dialog.show(supportFragmentManager, "LogoutConfirmationDialog")
    }
}
