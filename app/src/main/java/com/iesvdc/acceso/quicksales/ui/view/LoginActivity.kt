package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.ui.modelview.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra el formulario de login.
 *
 * - Si ya hay sesión iniciada, redirige a MenuActivity y finaliza.
 * - Permite ingresar correo y contraseña.
 * - Ofrece opción para mostrar/ocultar contraseña.
 * - Al presionar "Login", invoca LoginViewModel para autenticación.
 * - Al presionar "Registrar", navega a RegisterActivity.
 * - Observa LiveData de LoginViewModel para manejar éxito o error.
 */
@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var editTextUsuario: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegistrar: Button
    private lateinit var buttonTogglePassword: ImageButton
    private lateinit var textViewForgotPassword: TextView

    private var passwordVisible = false

    private val loginViewModel: LoginViewModel by viewModels()

    /**
     * Se ejecuta al crear la Activity.
     * - Ajusta barra de estado.
     * - Verifica si ya hay sesión iniciada; si es así, va a MenuActivity.
     * - Infla layout y llama a initViews, setupListeners y setupObservers.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Barra de estado en fondo claro con texto oscuro (API >= M)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.supeficie)
            window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Si ya está logueado, ir a menú principal
        if (loginViewModel.isLoggedIn()) {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)
        initViews()
        setupListeners()
        setupObservers()
    }

    /**
     * Inicializa referencias a vistas (EditText, Buttons, etc.).
     */
    private fun initViews() {
        editTextUsuario = findViewById(R.id.editTextUsuario)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonRegistrar = findViewById(R.id.buttonRegistrar)
        buttonTogglePassword = findViewById(R.id.buttonTogglePassword)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
    }

    /**
     * Configura listeners de clic:
     * - Toggle de visibilidad de contraseña.
     * - Botón Login: obtiene texto y llama a loginUser en ViewModel.
     * - Botón Registrar: abre RegisterActivity.
     */
    private fun setupListeners() {
        buttonTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            togglePasswordVisibility()
        }

        buttonLogin.setOnClickListener {
            val email = editTextUsuario.text.toString().trim()
            val password = editTextPassword.text.toString()
            loginViewModel.loginUser(email, password)
        }

        buttonRegistrar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    /**
     * Observa LiveData de loginSuccess y loginErrorMessage:
     * - Si loginSuccess es true, guarda sesión y navega a MenuActivity.
     * - Si hay mensaje de error, muestra Toast con el texto.
     */
    private fun setupObservers() {
        loginViewModel.loginSuccess.observe(this, Observer { success ->
            if (success == true) {
                loginViewModel.saveSession()
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            }
        })

        loginViewModel.loginErrorMessage.observe(this, Observer { message ->
            if (!message.isNullOrEmpty()) {
                showToast(message)
            }
        })
    }

    /**
     * Alterna la visibilidad de la contraseña en el EditText y cambia el icono del botón.
     */
    private fun togglePasswordVisibility() {
        if (passwordVisible) {
            editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            buttonTogglePassword.setImageResource(R.mipmap.ic_ojo_contrasenna_foreground)
        } else {
            editTextPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            buttonTogglePassword.setImageResource(R.mipmap.ic_ojo_cerrado_foreground)
        }
        editTextPassword.setSelection(editTextPassword.text.length)
    }

    /**
     * Muestra un Toast con el mensaje indicado.
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
