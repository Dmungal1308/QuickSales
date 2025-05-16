// File: com/iesvdc/acceso/quicksales/ui/view/WalletActivity.kt
package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.databinding.ActivityWalletBinding
import com.iesvdc.acceso.quicksales.ui.modelview.WalletViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWalletBinding
    private lateinit var drawerLayout: DrawerLayout
    private val viewModel: WalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWalletBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Inicializa el DrawerLayout
        drawerLayout = binding.drawerLayout

        // 2) Status bar en light mode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // 3) Ajusta insets (solo necesitas uno)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // 4) Observa el saldo
        viewModel.balance.observe(this) { saldo ->
            binding.textBalance.text = "€ %.2f".format(saldo)
        }

        // 5) Observa los resultados de operación (depositar/retirar)
        viewModel.operationResult.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // 6) Listener para depositar
        binding.buttonDeposit.setOnClickListener {
            val input = binding.editAmount.text.toString()
            Log.d("WalletActivity", "Deposit clicked, amount=$input")
            input.toBigDecimalOrNull()?.let { amount ->
                viewModel.deposit(amount)
            } ?: Toast.makeText(this, "Introduce un número válido", Toast.LENGTH_SHORT).show()
        }

        // 7) Listener para retirar
        binding.buttonWithdraw.setOnClickListener {
            val input = binding.editAmount.text.toString()
            Log.d("WalletActivity", "Withdraw clicked, amount=$input")
            input.toBigDecimalOrNull()?.let { amount ->
                viewModel.withdraw(amount)
            } ?: Toast.makeText(this, "Introduce un número válido", Toast.LENGTH_SHORT).show()
        }

        // 8) Toggle del drawer desde ambos iconos
        binding.btnMenu.setOnClickListener { toggleDrawer() }
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener { toggleDrawer() }

        // 9) Cerrar sesión desde el drawer
        findViewById<TextView>(R.id.cerrarSesion).setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // 10) Navegar de vuelta al menú principal
        binding.btnInicio.setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }
        binding.root.findViewById<TextView>(R.id.favoritos).setOnClickListener {
            startActivity(Intent(this, MisProductosActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        findViewById<ImageButton>(R.id.btnFavoritos).setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }
        findViewById<TextView>(R.id.textView3).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }
        binding.root.findViewById<TextView>(R.id.mis_productos)
            .setOnClickListener {
                startActivity(Intent(this, MisProductosActivity::class.java))
                drawerLayout.closeDrawer(GravityCompat.START)
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
