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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val vm: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = getColor(R.color.white)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        // observamos el perfil
        vm.profile.observe(this) { user ->
            if (user != null) {
                binding.textName.text = user.nombre
            }
            if (user != null) {
                binding.textUsername.text = user.nombreUsuario
            }
            if (user != null) {
                binding.textEmail.text = user.correo
            }
            binding.textPassword.text = "••••••••"
            if (user != null) {
                user.imagenBase64?.let {
                    val bytes = Base64.decode(it, Base64.DEFAULT)
                    Glide.with(this).asBitmap().load(bytes).into(binding.avatarImage)
                }
            }
        }

        findViewById<ImageButton>(R.id.botonFlecha).setOnClickListener {
            startActivity(Intent(this, MenuActivity::class.java))
        }

        binding.btnEditProfile.setOnClickListener {
            EditProfileDialogFragment().show(supportFragmentManager, "EditProfile")
        }
        binding.btnChangePassword.setOnClickListener {
            ChangePasswordDialogFragment().show(supportFragmentManager, "ChangePwd")
        }
        vm.errorToast.observe(this) { msg ->
            msg?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                vm.clearErrorToast()
            }
        }
    }
}
