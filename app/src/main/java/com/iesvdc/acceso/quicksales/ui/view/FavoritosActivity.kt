// File: com/iesvdc/acceso/quicksales/ui/view/FavoritosActivity.kt
package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.databinding.ActivityFavoritosBinding
import com.iesvdc.acceso.quicksales.ui.adapter.ProductAdapter
import com.iesvdc.acceso.quicksales.ui.modelview.FavoritosViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private lateinit var drawerLayout: DrawerLayout
    private val vm: FavoritosViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        adapter = ProductAdapter(
            onBuy = { vm.purchase(it) },
            onToggleFavorite = { vm.removeFavorite(it) } // en favoritos, togglear quita
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true.also {
                vm.filterByName(query ?: "")
            }
            override fun onQueryTextChange(newText: String?) = true.also {
                vm.filterByName(newText ?: "")
            }
        })


        // Drawer
        binding.imageButton.setOnClickListener { toggleDrawer() }
        binding.root.findViewById<ImageButton>(R.id.botonFlecha)
            .setOnClickListener { toggleDrawer() }
        binding.root.findViewById<TextView>(R.id.cerrarSesion)
            .setOnClickListener { showLogoutConfirmationDialog() }
        binding.root.findViewById<TextView>(R.id.mis_productos)
            .setOnClickListener {
                startActivity(Intent(this, MisProductosActivity::class.java))
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


        vm.favoriteIds.observe(this) { favSet ->
            adapter.setFavorites(favSet)
        }

        vm.products.observe(this) { lista ->
            adapter.submitList(lista) {
                adapter.setFavorites(vm.favoriteIds.value.orEmpty())
            }
        }

        vm.logoutEvent.observe(this) {
            if (it) {
                vm.resetLogoutEvent()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = LogoutConfirmationDialogFragment()
        dialog.onLogoutConfirmed = {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        dialog.show(supportFragmentManager, "LogoutConfirmationDialog")
    }
}
