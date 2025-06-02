package com.iesvdc.acceso.quicksales.ui.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.iesvdc.acceso.quicksales.R
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserDetailResponse
import com.iesvdc.acceso.quicksales.databinding.UserDetailBinding
import com.iesvdc.acceso.quicksales.ui.modelview.UsersViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.DeleteUserDialogFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity que muestra los detalles de un usuario específico.
 *
 * - Recibe un JSON con los datos de [UserDetailResponse] en el extra "user".
 * - Despliega nombre, nombre de usuario, correo, rol y saldo.
 * - Decodifica y muestra la imagen en Base64 si existe.
 * - Permite eliminar al usuario mediante un diálogo de confirmación.
 * - El botón de retroceso regresa a [UsersActivity].
 */
@AndroidEntryPoint
class UserDetailActivity : AppCompatActivity() {

    private lateinit var binding: UserDetailBinding
    private val vm: UsersViewModel by viewModels()

    /**
     * Configura la UI al crear la Activity:
     * 1. Infla el layout con ViewBinding.
     * 2. Ajusta la barra de estado a blanco con texto oscuro si la API lo soporta.
     * 3. Obtiene el JSON del usuario desde el Intent y lo parsea a [UserDetailResponse].
     * 4. Llena los campos de texto con los datos del usuario.
     * 5. Decodifica la imagen en Base64 y la carga en el ImageView con Glide.
     * 6. Configura el botón de eliminar para mostrar [DeleteUserDialogFragment] y llamar a vm.removeUser().
     * 7. Configura el botón de retroceso para volver a [UsersActivity].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar barra de estado en blanco con texto oscuro si API >= M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // Obtener el JSON del usuario desde el Intent; si falta, lanza excepción
        val json = intent.getStringExtra("user")
            ?: throw IllegalArgumentException("Falta extra 'user'")
        val user = Gson().fromJson(json, UserDetailResponse::class.java)

        // Mostrar datos en los TextView correspondientes
        binding.tvName.text     = user.nombre
        binding.tvUsername.text = user.nombreUsuario
        binding.tvEmail.text    = user.correo
        binding.tvRole.text     = user.rol
        binding.tvBalance.text  = "€ %.2f".format(user.saldo)

        // Decodificar imagen en Base64 y cargarla con Glide si existe
        user.imagenBase64?.let { b64 ->
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            Glide.with(this)
                .load(bytes)
                .circleCrop()
                .into(binding.imgUser)
        }

        // Botón de eliminar usuario: muestra diálogo de confirmación y, si confirma, llama a vm.removeUser()
        binding.btnDelete.setOnClickListener {
            DeleteUserDialogFragment {
                vm.removeUser(user.id)
                finish()
            }.show(supportFragmentManager, "delete_user")
        }

        // Botón de retroceso: abre UsersActivity
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener {
            startActivity(Intent(this, UsersActivity::class.java))
        }
    }
}
