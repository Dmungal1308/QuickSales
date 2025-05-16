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
import com.iesvdc.acceso.quicksales.databinding.ActivityMisProductosBinding
import com.iesvdc.acceso.quicksales.ui.adapter.MisProductosAdapter
import com.iesvdc.acceso.quicksales.ui.modelview.MisProductosViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MisProductosActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMisProductosBinding
    private lateinit var drawerLayout: DrawerLayout
    private val vm: MisProductosViewModel by viewModels()

    private val adapter = MisProductosAdapter(
        onEdit   = { product ->
            EditProductDialogFragment(product)
                .show(supportFragmentManager, "EditProductDialog")
        },
        onDelete = { product ->
            DeleteProductDialogFragment(product.id)
                .show(supportFragmentManager, "DeleteProductDialog")
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerLayout = binding.drawerLayout

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            AddProductDialogFragment().show(supportFragmentManager, "AddProductDialog")
        }

        // *** NUEVO: configuración de SearchView ***
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

        vm.products.observe(this) { list ->
            adapter.submitList(list)
        }

        binding.imageButton.setOnClickListener { toggleDrawer() }
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener { toggleDrawer() }
        findViewById<TextView>(R.id.cerrarSesion).setOnClickListener { showLogoutConfirmationDialog() }
        findViewById<TextView>(R.id.mis_productos).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<TextView>(R.id.cartera).setOnClickListener {
            startActivity(Intent(this, WalletActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<TextView>(R.id.textView3).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.root.findViewById<TextView>(R.id.favoritos).setOnClickListener {
            startActivity(Intent(this, MisProductosActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<ImageButton>(R.id.btnFavoritos).setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnInicio).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
    }

    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START)
        else
            drawerLayout.openDrawer(GravityCompat.START)
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
