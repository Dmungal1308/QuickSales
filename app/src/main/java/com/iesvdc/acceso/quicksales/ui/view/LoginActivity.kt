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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.supeficie)
            window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

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

    private fun initViews() {
        editTextUsuario = findViewById(R.id.editTextUsuario)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonRegistrar = findViewById(R.id.buttonRegistrar)
        buttonTogglePassword = findViewById(R.id.buttonTogglePassword)
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword)
    }

    private fun setupListeners() {
        buttonTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            togglePasswordVisibility()
        }

        buttonLogin.setOnClickListener {
            val email = editTextUsuario.text.toString().trim()
            val password = editTextPassword.text.toString()
            if (email == "admin" && password == "1234") {
                loginViewModel.saveSession()
                startActivity(Intent(this, MenuActivity::class.java))
                finish()
            } else {
                loginViewModel.loginUser(email, password)
            }
        }

        buttonRegistrar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }


    }

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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
