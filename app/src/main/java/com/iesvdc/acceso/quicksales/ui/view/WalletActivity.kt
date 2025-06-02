package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.databinding.ActivityWalletBinding
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import com.iesvdc.acceso.quicksales.ui.modelview.WalletViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.LogoutConfirmationDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que permite al usuario gestionar su cartera:
 * - Muestra el saldo actual.
 * - Permite depositar y retirar montos.
 * - Incluye un DrawerLayout para navegar a otras pantallas (Mis Productos, Favoritos, etc.).
 * - Configura botones para depósito, retiro, navegación y logout.
 * - Observa LiveData de [WalletViewModel] para actualizar saldo y mostrar resultados de operaciones.
 */
@AndroidEntryPoint
class WalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletBinding
    private lateinit var drawerLayout: DrawerLayout

    /** ViewModel que maneja el saldo y las operaciones de depósito/retiro. */
    private val viewModel: WalletViewModel by viewModels()

    /** ViewModel que proporciona información de perfil para mostrar avatar y rol. */
    private val settingsVm: SettingsViewModel by viewModels()

    /**
     * Se ejecuta al crear la Activity:
     * 1. Infla el layout mediante ViewBinding.
     * 2. Configura la barra de estado a blanco con texto oscuro si la API lo permite.
     * 3. Ajusta padding para no solapar barras del sistema.
     * 4. Observa el LiveData de saldo para mostrar el valor formateado.
     * 5. Observa el LiveData de resultado de operación para mostrar Toast con mensaje.
     * 6. Configura botones:
     *    - Deposit: valida y llama a viewModel.deposit.
     *    - Withdraw: valida y llama a viewModel.withdraw.
     * 7. Configura DrawerLayout y navegación en el menú lateral.
     * 8. Observa LiveData de perfil para mostrar u ocultar opción "Usuarios" y cargar avatar.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout = binding.drawerLayout

        // Configurar barra de estado en blanco con texto oscuro si API >= M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Ajustar padding para barras del sistema (status/navigation)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Mostrar saldo actual formateado al observar LiveData
        viewModel.balance.observe(this) { saldo ->
            binding.textBalance.text = "€ %.2f".format(saldo)
        }

        // Mostrar mensaje de resultado de operaciones (depósito/retiro) en Toast
        viewModel.operationResult.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Configurar botón "Deposit": obtiene monto, valida y realiza depósito
        binding.buttonDeposit.setOnClickListener {
            val input = binding.editAmount.text.toString()
            Log.d("WalletActivity", "Deposit clicked, amount=$input")
            input.toBigDecimalOrNull()?.let { amount ->
                viewModel.deposit(amount)
            } ?: Toast.makeText(this, "Introduce un número válido", Toast.LENGTH_SHORT).show()
        }

        // Configurar botón "Withdraw": obtiene monto, valida y realiza retiro
        binding.buttonWithdraw.setOnClickListener {
            val input = binding.editAmount.text.toString()
            Log.d("WalletActivity", "Withdraw clicked, amount=$input")
            input.toBigDecimalOrNull()?.let { amount ->
                viewModel.withdraw(amount)
            } ?: Toast.makeText(this, "Introduce un número válido", Toast.LENGTH_SHORT).show()
        }

        // Configurar botón de menú para abrir/cerrar DrawerLayout
        binding.btnMenu.setOnClickListener { toggleDrawer() }
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener { toggleDrawer() }

        // Botón de "Inicio" regresa a MenuActivity y cierra esta Activity
        binding.btnInicio.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        // Configurar navegación en el Drawer
        findViewById<TextView>(R.id.cerrarSesion).setOnClickListener {
            showLogoutConfirmationDialog()
        }
        binding.root.findViewById<TextView>(R.id.favoritos).setOnClickListener {
            startActivity(Intent(this, MyProductsActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<ImageButton>(R.id.btnChat).setOnClickListener {
            startActivity(Intent(this, ChatRecopiladosActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnFavoritos).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }
        findViewById<TextView>(R.id.textView3).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<ImageButton>(R.id.imageButton3).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.root.findViewById<TextView>(R.id.mis_productos).setOnClickListener {
            startActivity(Intent(this, MyProductsActivity::class.java))
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

        // Mostrar opción "Usuarios" solo si el rol es "admin"
        val tvUsuarios = binding.root.findViewById<TextView>(R.id.usuarios)
        settingsVm.profile.observe(this) { me ->
            tvUsuarios.visibility = if (me?.rol == "admin") View.VISIBLE else View.GONE
        }
        tvUsuarios.setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // Cargar avatar del usuario en el Drawer y botón superior mediante Glide si existe
        val navUserButton = binding.root.findViewById<ImageButton>(R.id.botonUsuario)
        settingsVm.profile.observe(this) { user ->
            val imageBytes = user?.imagenBase64?.let { Base64.decode(it, Base64.DEFAULT) }
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
     * Alterna el estado del DrawerLayout (abrir o cerrar).
     */
    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    /**
     * Muestra un diálogo de confirmación de logout. Si se confirma, navega a LoginActivity
     * y cierra esta Activity.
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
