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

/**
 * ViewModel encargado de gestionar la lógica de registro de nuevos usuarios.
 *
 * Utiliza [RegisterUserUseCase] para ejecutar el registro en segundo plano y expone LiveData
 * que notifica a la UI sobre el resultado (éxito o error) de la operación.
 *
 * @property registerUserUseCase Caso de uso que realiza la llamada al repositorio para registrar al usuario.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUserUseCase: RegisterUserUseCase
) : ViewModel() {

    /**
     * LiveData que indica si el registro fue exitoso.
     * - `true` cuando se completó correctamente.
     * - `false` cuando hubo un error.
     */
    private val _registrationSuccess = MutableLiveData<Boolean>()
    val registrationSuccess: LiveData<Boolean> get() = _registrationSuccess

    /**
     * LiveData que contiene el mensaje de error en caso de fallo al registrar.
     * - `null` si no hay error o después de un registro exitoso.
     * - Cadena con descripción del error en caso de fallo.
     */
    private val _registrationError = MutableLiveData<String?>()
    val registrationError: LiveData<String?> get() = _registrationError

    /**
     * Inicia el proceso de registro de usuario con los datos proporcionados.
     *
     * - Lanza una corrutina en [viewModelScope] para invocar [registerUserUseCase].
     * - Si el resultado es [RegistrationResult.Success], publica `true` en [_registrationSuccess]
     *   y `null` en [_registrationError].
     * - Si el resultado es [RegistrationResult.Error], publica `false` en [_registrationSuccess]
     *   y el mensaje de error en [_registrationError].
     *
     * @param nombreUsuario Nombre de usuario único.
     * @param nombreCompleto Nombre completo del usuario.
     * @param correo Correo electrónico del usuario.
     * @param contrasena Contraseña elegida.
     * @param repeatPassword Confirmación de la contraseña.
     */
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
