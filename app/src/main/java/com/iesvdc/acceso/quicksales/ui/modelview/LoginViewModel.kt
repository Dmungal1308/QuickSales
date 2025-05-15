package com.iesvdc.acceso.quicksales.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iesvdc.acceso.quicksales.domain.models.UserData
import com.iesvdc.acceso.quicksales.domain.usercase.LoginResult
import com.iesvdc.acceso.quicksales.domain.usercase.LoginUserUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.SaveSessionUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.IsLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el login, adaptado al nuevo UserData.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    private val _loginErrorMessage = MutableLiveData<String?>()
    val loginErrorMessage: LiveData<String?> get() = _loginErrorMessage

    private var loggedUser: UserData? = null

    /**
     * Ejecuta el caso de uso de login con correo y contraseña.
     */
    fun loginUser(correo: String, contrasena: String) {
        viewModelScope.launch {
            when (val result = loginUserUseCase(correo, contrasena)) {
                is LoginResult.Success -> {
                    val user = result.user
                    loggedUser = user
                    // Guardar sesión con ID de usuario
                    saveSessionUseCase(user.id)
                    _loginSuccess.value = true
                    _loginErrorMessage.value = null
                }
                is LoginResult.Error -> {
                    _loginSuccess.value = false
                    _loginErrorMessage.value = result.message
                }
                LoginResult.EmailNotVerified -> {
                    _loginSuccess.value = false
                    _loginErrorMessage.value = "Email no verificado"
                }
            }
        }
    }

    /**
     * Guarda sesión manualmente si ya hay usuario logueado.
     */
    fun saveSession() {
        loggedUser?.let { user ->
            saveSessionUseCase(user.id)
        }
    }

    /**
     * Comprueba si ya existe sesión.
     */
    fun isLoggedIn(): Boolean {
        return isLoggedInUseCase()
    }
}
