package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iesvdc.acceso.quicksales.domain.usercase.login.RegisterUserUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.RegistrationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> get() = _registrationSuccess

    private val _registrationError = MutableLiveData<String?>()
    val registrationError: LiveData<String?> get() = _registrationError


    fun registerUser(
        nombreUsuario: String,
        nombreCompleto: String,
        correo: String,
        contrasena: String,
        repeatPassword: String
    ) {
        viewModelScope.launch {
            when (val result = registerUserUseCase(
                usuario = nombreUsuario,
                nombreCompleto = nombreCompleto,
                correo = correo,
                password = contrasena,
                repeatPassword = repeatPassword
            )) {
                is RegistrationResult.Success -> {
                    _registrationSuccess.value = true
                    _registrationError.value = null
                }
                is RegistrationResult.Error -> {
                    _registrationSuccess.value = false
                    _registrationError.value = result.message
                }
            }
        }
    }
}
