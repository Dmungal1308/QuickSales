package com.iesvdc.acceso.quicksales.domain.usercase.login


import com.iesvdc.acceso.quicksales.data.repository.AuthRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        usuario: String,
        nombreCompleto: String,
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

        val result = authRepository.register(
            nombre        = nombreCompleto,
            nombreUsuario = usuario,
            contrasena    = password,
            correo        = correo
        )
        return result
    }
}


