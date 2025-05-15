package com.iesvdc.acceso.quicksales.domain.usercase


import com.iesvdc.acceso.quicksales.data.repository.AuthRepository
import javax.inject.Inject


import android.util.Log

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        usuario: String,          // se corresponderá con nombreUsuario
        nombreCompleto: String,   // se corresponderá con nombre
        correo: String,
        password: String,
        repeatPassword: String
    ): RegistrationResult {
        if (correo.isBlank() || password.isBlank()) {
            return RegistrationResult.Error("Por favor, completa todos los campos.")
        }
        if (password != repeatPassword) {
            return RegistrationResult.Error("Las contraseñas no coinciden.")
        }

        // Ahora pasamos los parámetros en el orden correcto:
        val result = authRepository.register(
            nombre        = nombreCompleto,
            nombreUsuario = usuario,
            contrasena    = password,
            correo        = correo
            // puedes omitir imagenBase64 y rol si no los usas aquí
        )
        return result
    }
}


