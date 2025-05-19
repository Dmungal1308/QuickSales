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

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase
) : ViewModel() {
    private val _profile = MutableLiveData<UserResponse?>()
    val profile: LiveData<UserResponse?> = _profile

    private val _errorToast = MutableLiveData<String?>()
    val errorToast: LiveData<String?> = _errorToast

    init {
        loadProfile()
    }

    private fun loadProfile() = viewModelScope.launch {
        _profile.value = getProfileUseCase()
    }

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

    fun changePassword(newPassword: String) = viewModelScope.launch {
        changePasswordUseCase(newPassword)
    }

    fun clearErrorToast() {
        _errorToast.value = null
    }
}
