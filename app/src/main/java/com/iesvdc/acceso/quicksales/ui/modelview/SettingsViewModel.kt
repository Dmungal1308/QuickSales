package com.iesvdc.acceso.quicksales.ui.modelview

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserResponse
import com.iesvdc.acceso.quicksales.domain.usercase.login.ChangePasswordUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.GetProfileUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica de la pantalla de ajustes de usuario,
 * incluyendo la obtención y actualización del perfil, así como el cambio de contraseña.
 *
 * Utiliza los casos de uso:
 *  - [GetProfileUseCase] para recuperar la información actual del usuario.
 *  - [UpdateProfileUseCase] para actualizar los datos del perfil, incluyendo la foto en Base64.
 *  - [ChangePasswordUseCase] para cambiar la contraseña del usuario.
 *
 * Expone LiveData para:
 *  - [profile]: la información del usuario cargada.
 *  - [errorToast]: mensajes de error que deben mostrarse en un Toast.
 *
 * Al inicializarse, carga automáticamente el perfil del usuario llamando a [loadProfile].
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {

    /**
     * LiveData que contiene la información del perfil del usuario.
     * - Se actualiza al cargar o actualizar el perfil.
     * - La UI debe observar este valor para mostrar los datos en pantalla.
     */
    private val _profile = MutableLiveData<UserResponse?>()
    val profile: LiveData<UserResponse?> = _profile

    /**
     * LiveData que contiene mensajes de error que se deben mostrar al usuario.
     * - Se utiliza para notificar fallos al actualizar el perfil.
     * - La UI debe observar este valor para mostrar un Toast con el mensaje.
     */
    private val _errorToast = MutableLiveData<String?>()
    val errorToast: LiveData<String?> = _errorToast

    init {
        // Al inicializar el ViewModel, cargamos el perfil del usuario.
        loadProfile()
    }

    /**
     * Carga la información del perfil del usuario actual.
     *
     * - Ejecuta [getProfileUseCase] en una corrutina de [viewModelScope].
     * - Publica el resultado en [_profile].
     */
    private fun loadProfile() = viewModelScope.launch {
        _profile.value = getProfileUseCase()
    }

    /**
     * Actualiza el perfil del usuario con los datos proporcionados.
     *
     * @param nombre Nombre completo del usuario.
     * @param nombreUsuario Nombre de usuario único.
     * @param correo Correo electrónico del usuario.
     * @param imagenBase64 Cadena Base64 de la nueva imagen de perfil, o null si no cambia.
     *
     * - Ejecuta [updateProfileUseCase] en una corrutina de [viewModelScope].
     * - Si la actualización es exitosa, publica el usuario actualizado en [_profile].
     * - Si ocurre un [HttpException], extrae el cuerpo de error y publica un mensaje en [_errorToast].
     * - Si ocurre cualquier otra excepción, registra el error en Log y publica un mensaje genérico en [_errorToast].
     */
    fun updateProfile(
        nombre: String,
        nombreUsuario: String,
        correo: String,
        imagenBase64: String?
    ) = viewModelScope.launch {
        try {
            val updatedUser = updateProfileUseCase(nombre, nombreUsuario, correo, imagenBase64)
            _profile.value = updatedUser
        } catch (e: HttpException) {
            val errBody = e.response()?.errorBody()?.string()
            Log.e("SettingsViewModel", "Error al actualizar perfil: $errBody")
            _errorToast.value = "No se ha podido guardar la foto: $errBody"
        } catch (e: Exception) {
            Log.e("SettingsViewModel", "Error inesperado", e)
            _errorToast.value = "Error inesperado: ${e.localizedMessage}"
        }
    }

    /**
     * Cambia la contraseña del usuario actual.
     *
     * @param newPassword Nueva contraseña a establecer.
     *
     * - Ejecuta [changePasswordUseCase] en una corrutina de [viewModelScope].
     * - No expone resultado directamente; se asume manejo en la capa de dominio o UI.
     */
    fun changePassword(newPassword: String) = viewModelScope.launch {
        changePasswordUseCase(newPassword)
    }

    /**
     * Limpia el mensaje de error de Toast, estableciendo [_errorToast] a null.
     *
     * - Debe llamarse después de que la UI haya mostrado el mensaje para evitar mostrarlo nuevamente.
     */
    fun clearErrorToast() {
        _errorToast.value = null
    }
}
