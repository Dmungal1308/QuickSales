package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserDetailResponse
import com.iesvdc.acceso.quicksales.domain.usercase.login.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.DeleteUserUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.GetAllUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsuariosViewModel @Inject constructor(
    private val getAllUsers: GetAllUsersUseCase,
    private val deleteUser: DeleteUserUseCase,
    private val logoutUseCase: LogoutUseCase
): ViewModel() {
    private val _users = MutableLiveData<List<UserDetailResponse>?>()
    val users: MutableLiveData<List<UserDetailResponse>?> = _users
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    init { loadUsers() }
    fun loadUsers() = viewModelScope.launch {
        _users.value = getAllUsers()
    }
    fun removeUser(id: Int) = viewModelScope.launch {
        if (deleteUser(id)) loadUsers()
    }
    fun logout() { logoutUseCase(); _logoutEvent.value = true }
    fun resetLogoutEvent() { _logoutEvent.value = false }
}

