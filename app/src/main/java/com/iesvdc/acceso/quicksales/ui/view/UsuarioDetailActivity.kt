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
import com.iesvdc.acceso.quicksales.ui.modelview.UsuariosViewModel
import com.iesvdc.acceso.quicksales.ui.view.dialog.DeleteUserDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UsuarioDetailActivity : AppCompatActivity() {

    private lateinit var binding: UserDetailBinding
    private val vm: UsuariosViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val json = intent.getStringExtra("user")
            ?: throw IllegalArgumentException("Falta extra 'user'")
        val user = Gson().fromJson(json, UserDetailResponse::class.java)

        binding.tvName.text        = user.nombre
        binding.tvUsername.text    = user.nombreUsuario
        binding.tvEmail.text       = user.correo
        binding.tvRole.text        = user.rol
        binding.tvBalance.text     = "€ %.2f".format(user.saldo)

        user.imagenBase64?.let { b64 ->
            val bytes = Base64.decode(b64, Base64.DEFAULT)
            Glide.with(this)
                .load(bytes)
                .circleCrop()
                .into(binding.imgUser)
        }

        binding.btnDelete.setOnClickListener {
            DeleteUserDialogFragment {
                vm.removeUser(user.id)
                finish()
            }.show(supportFragmentManager, "delete_user")
        }
        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener {
            startActivity(Intent(this, UsuariosActivity::class.java))
        }
    }
}
