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

/**
 * ViewModel encargado de gestionar la lógica de inicio de sesión de usuarios.
 *
 * Utiliza casos de uso para:
 *  - Ejecutar la autenticación del usuario ([LoginUserUseCase]).
 *  - Guardar la sesión del usuario en almacenamiento local ([SaveSessionUseCase]).
 *  - Verificar si ya existe una sesión activa ([IsLoggedInUseCase]).
 *
 * Expone LiveData para notificar la vista acerca del estado de la operación:
 *  - [_loginSuccess]: indica si el inicio de sesión fue exitoso.
 *  - [_loginErrorMessage]: contiene un mensaje de error en caso de fallo.
 *
 * @property loginUserUseCase Caso de uso para autenticar al usuario con correo y contraseña.
 * @property saveSessionUseCase Caso de uso para almacenar el ID de usuario en sesión activa.
 * @property isLoggedInUseCase Caso de uso para comprobar si el usuario ya está logueado.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUserUseCase: LoginUserUseCase,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    /**
     * LiveData que notifica si el inicio de sesión fue exitoso.
     *  - true: credenciales correctas y sesión guardada.
     *  - false: credenciales inválidas u otro error.
     */
    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess

    /**
     * LiveData que contiene el mensaje de error en caso de que la autenticación falle.
     *  - null: no hay error o se receteó después de éxito.
     *  - cadena con descripción del error en caso de fallo.
     */
    private val _loginErrorMessage = MutableLiveData<String?>()
    val loginErrorMessage: LiveData<String?> get() = _loginErrorMessage

    /** Usuario autenticado actualmente, si la operación fue exitosa. */
    private var loggedUser: UserData? = null

    /**
     * Intenta iniciar sesión con las credenciales proporcionadas.
     *
     * - Llama a [loginUserUseCase] con los parámetros recibidos.
     * - Si el resultado es [LoginResult.Success]:
     *     1. Se guarda el usuario devuelto en [loggedUser].
     *     2. Se invoca [saveSessionUseCase] para almacenar la sesión.
     *     3. Se publica `true` en [_loginSuccess] y `null` en [_loginErrorMessage].
     * - Si el resultado es [LoginResult.Error]:
     *     1. Se publica `false` en [_loginSuccess].
     *     2. Se coloca el mensaje de error en [_loginErrorMessage].
     * - Si el resultado es [LoginResult.EmailNotVerified]:
     *     1. Se publica `false` en [_loginSuccess].
     *     2. Se coloca el mensaje "Email no verificado" en [_loginErrorMessage].
     *
     * @param correo Correo electrónico ingresado por el usuario.
     * @param contrasena Contraseña ingresada por el usuario.
     */
    fun loginUser(correo: String, contrasena: String) {
        viewModelScope.launch {
            when (val result = loginUserUseCase(correo, contrasena)) {
                is LoginResult.Success -> {
                    val user = result.user
                    loggedUser = user
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
     * Guarda la sesión del usuario actualmente autenticado.
     *
     * Solo se invoca si [loggedUser] no es nulo.
     * Utiliza [saveSessionUseCase] para almacenar el ID de usuario en un repositorio local o SharedPreferences.
     */
    fun saveSession() {
        loggedUser?.let { user ->
            saveSessionUseCase(user.id)
        }
    }

    /**
     * Verifica si ya existe una sesión de usuario activa.
     *
     * Llama a [isLoggedInUseCase], que retorna `true` si el usuario está logueado,
     * o `false` si no hay sesión activa.
     *
     * @return `true` si el usuario ya inició sesión; `false` en caso contrario.
     */
    fun isLoggedIn(): Boolean {
        return isLoggedInUseCase()
    }
}
