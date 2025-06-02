package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.ui.modelview.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra el formulario de registro de usuario.
 *
 * Funcionalidades:
 * - Permite ingresar nombre de usuario, nombre completo, correo, contraseña y confirmación.
 * - Al presionar "Registrar", invoca [RegisterViewModel.registerUser] con los datos ingresados.
 * - Observa LiveData de [RegisterViewModel] para:
 *   - Mostrar Toast de éxito y navegar a [LoginActivity].
 *   - Mostrar Toast de error si el registro falla.
 */
@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextUsuario: EditText
    private lateinit var editTextNombreApellidos: EditText
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextRepeatPassword: EditText
    private lateinit var buttonRegistrar: Button

    private val registerViewModel: RegisterViewModel by viewModels()

    /**
     * Se ejecuta al crear la Activity:
     * - Configura la barra de estado en blanco con texto oscuro (API >= M).
     * - Inicializa las vistas y configura observadores.
     * - Maneja el clic en el botón "Registrar" para invocar el ViewModel.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        initViews()
        setupObservers()

        buttonRegistrar.setOnClickListener {
            Log.d("RegistrarActivity", "Botón registrar clickeado")
            registerViewModel.registerUser(
                nombreUsuario  = editTextUsuario.text.toString(),
                nombreCompleto = editTextNombreApellidos.text.toString(),
                correo         = editTextCorreo.text.toString(),
                contrasena     = editTextPassword.text.toString(),
                repeatPassword = editTextRepeatPassword.text.toString()
            )
        }
    }

    /**
     * Inicializa referencias a los EditText y Button definidos en el layout.
     */
    private fun initViews() {
        editTextUsuario         = findViewById(R.id.editTextUsuario)
        editTextNombreApellidos = findViewById(R.id.editTextNombreApellidos)
        editTextCorreo          = findViewById(R.id.editTextCorreo)
        editTextPassword        = findViewById(R.id.editTextPassword)
        editTextRepeatPassword  = findViewById(R.id.editTextRepeatPassword)
        buttonRegistrar         = findViewById(R.id.buttonRegistrar)
    }

    /**
     * Configura observadores de LiveData en [registerViewModel]:
     * - registrationSuccess: muestra Toast de éxito y navega a LoginActivity.
     * - registrationError: muestra Toast con el mensaje de error.
     */
    private fun setupObservers() {
        registerViewModel.registrationSuccess.observe(this, Observer { success ->
            if (success == true) {
                showToast("Registro exitoso.")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        })

        registerViewModel.registrationError.observe(this, Observer { error ->
            error?.let { showToast(it) }
        })
    }

    /**
     * Muestra un Toast con el mensaje proporcionado.
     *
     * @param message Texto a mostrar en el Toast.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
