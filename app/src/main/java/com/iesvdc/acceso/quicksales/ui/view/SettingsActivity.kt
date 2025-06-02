package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.databinding.ActivitySettingsBinding
import com.iesvdc.acceso.quicksales.ui.modelview.SettingsViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.ChangePasswordDialogFragment
import com.iesvdc.acceso.quicksales.ui.view.dialog.EditProfileDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra y permite editar los ajustes del perfil de usuario.
 *
 * Funcionalidades:
 * 1. Observa LiveData de [SettingsViewModel.profile] para llenar campos con datos del usuario:
 *    - Nombre completo, nombre de usuario, correo electrónico y avatar (imagen en Base64).
 *    - Muestra la contraseña como puntos fijos.
 * 2. Configura el botón de retroceso (botón flecha) para regresar al [MenuActivity].
 * 3. Permite abrir diálogos:
 *    - [EditProfileDialogFragment] para editar datos de perfil.
 *    - [ChangePasswordDialogFragment] para cambiar la contraseña.
 * 4. Observa LiveData de [SettingsViewModel.errorToast] para mostrar errores en Toast.
 */
@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    /** Binding para acceder a vistas del layout activity_settings.xml. */
    private lateinit var binding: ActivitySettingsBinding

    /** ViewModel que maneja la lógica de obtención y actualización del perfil. */
    private val vm: SettingsViewModel by viewModels()

    /**
     * Se ejecuta al crear la Activity:
     * 1. Infla el layout con ViewBinding.
     * 2. Ajusta la barra de estado a blanco con texto oscuro si la API lo soporta (Android M+).
     * 3. Observa los datos de perfil en el ViewModel para mostrarlos en pantalla:
     *    - Nombre, nombre de usuario, correo y contraseña oculta.
     *    - Decodifica la imagen en Base64 y la carga en el ImageView con Glide.
     * 4. Configura el botón de flecha para volver a [MenuActivity].
     * 5. Configura los botones:
     *    - Editar perfil: abre [EditProfileDialogFragment].
     *    - Cambiar contraseña: abre [ChangePasswordDialogFragment].
     * 6. Observa errores en LiveData y los muestra en un Toast.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Barra de estado en blanco con texto oscuro (API >= M)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Observar perfil del usuario para llenar campos
        vm.profile.observe(this) { user ->
            if (user != null) {
                // Mostrar nombre completo
                binding.textName.text = user.nombre
                // Mostrar nombre de usuario
                binding.textUsername.text = user.nombreUsuario
                // Mostrar correo electrónico
                binding.textEmail.text = user.correo
            }
            // Mostrar contraseña oculta siempre como puntos
            binding.textPassword.text = "••••••••"
            if (user != null) {
                // Decodificar avatar en Base64 y cargar con Glide
                user.imagenBase64?.let {
                    val bytes = Base64.decode(it, Base64.DEFAULT)
                    Glide.with(this).asBitmap().load(bytes).into(binding.avatarImage)
                }
            }
        }

        // Botón de flecha para volver al menú principal
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }

        // Botón para editar perfil: abre diálogo correspondiente
        binding.btnEditProfile.setOnClickListener {
            EditProfileDialogFragment().show(supportFragmentManager, "EditProfile")
        }
        // Botón para cambiar contraseña: abre diálogo correspondiente
        binding.btnChangePassword.setOnClickListener {
            ChangePasswordDialogFragment().show(supportFragmentManager, "ChangePwd")
        }

        // Observar errores desde el ViewModel y mostrarlos en Toast
        vm.errorToast.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                vm.clearErrorToast()
            }
        }
    }
}
