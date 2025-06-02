package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.ui.modelview.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoadingScreenActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    /**
     * Al crear la Activity:
     * - Infla el layout de pantalla de carga.
     * - Configura la barra de estado en blanco con texto oscuro si la API lo permite.
     * - Ajusta el padding del root view para no superponer barras del sistema.
     * - Observa [SplashViewModel.navigateToLoginEvent] para navegar a LoginActivity
     *   cuando el evento emita `true`.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_carga)

        // Barra de estado en blanco con texto oscuro si API >= M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Ajustar padding para barras del sistema
        val rootView = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Navegar a LoginActivity cuando SplashViewModel emita true
        splashViewModel.navigateToLoginEvent.observe(this) { shouldNavigate ->
            if (shouldNavigate == true) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}
