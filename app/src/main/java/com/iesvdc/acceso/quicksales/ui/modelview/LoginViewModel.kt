package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iesvdc.acceso.quicksales.domain.models.UserData
import com.iesvdc.acceso.quicksales.domain.usercase.login.LoginResult
import com.iesvdc.acceso.quicksales.domain.usercase.login.LoginUserUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.SaveSessionUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.IsLoggedInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


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


    fun saveSession() {
        loggedUser?.let { user ->
            saveSessionUseCase(user.id)
        }
    }


    fun isLoggedIn(): Boolean {
        return isLoggedInUseCase()
    }
}
