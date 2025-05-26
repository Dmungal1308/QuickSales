// File: com/iesvdc/acceso/quicksales/ui/view/RegistrarActivity.kt
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

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextUsuario: EditText
    private lateinit var editTextNombreApellidos: EditText
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextRepeatPassword: EditText
    private lateinit var buttonRegistrar: Button

    private val registerViewModel: RegisterViewModel by viewModels()

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
                nombreUsuario   = editTextUsuario.text.toString(),
                nombreCompleto  = editTextNombreApellidos.text.toString(),
                correo          = editTextCorreo.text.toString(),
                contrasena      = editTextPassword.text.toString(),
                repeatPassword  = editTextRepeatPassword.text.toString()
            )
        }
    }

    private fun initViews() {
        editTextUsuario           = findViewById(R.id.editTextUsuario)
        editTextNombreApellidos   = findViewById(R.id.editTextNombreApellidos)
        editTextCorreo            = findViewById(R.id.editTextCorreo)
        editTextPassword          = findViewById(R.id.editTextPassword)
        editTextRepeatPassword    = findViewById(R.id.editTextRepeatPassword)
        buttonRegistrar           = findViewById(R.id.buttonRegistrar)
    }

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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
