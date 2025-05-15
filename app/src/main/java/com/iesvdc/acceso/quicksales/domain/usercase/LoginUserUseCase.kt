package com.iesvdc.acceso.quicksales.domain.usercase

import com.iesvdc.acceso.quicksales.data.repository.AuthRepository
import javax.inject.Inject
import android.util.Log
import com.iesvdc.acceso.quicksales.domain.models.UserData

sealed class LoginResult {
    data class Success(val user: UserData) : LoginResult()
    data class Error(val message: String) : LoginResult()
    object EmailNotVerified : LoginResult()
}


class LoginUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): LoginResult {
        if (email.isBlank() || password.isBlank()) {
            Log.d("LoginUserUseCase", "Campos vacíos")
            return LoginResult.Error("Por favor, completa todos los campos.")
        }
        Log.d("LoginUserUseCase", "Invoking login for email: $email")
        return authRepository.login(email, password)
    }
}
