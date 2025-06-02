package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserDetailResponse
import com.iesvdc.acceso.quicksales.domain.usercase.login.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.DeleteUserUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.GetAllUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica relacionada con la lista de usuarios en la pantalla de administración.
 *
 * Utiliza los casos de uso:
 *  - [GetAllUsersUseCase] para obtener el listado completo de usuarios.
 *  - [DeleteUserUseCase] para eliminar un usuario del sistema.
 *  - [LogoutUseCase] para cerrar la sesión del usuario actual.
 *
 * Expone LiveData para:
 *  - [_users]: lista de [UserDetailResponse] obtenida desde el servidor.
 *  - [_logoutEvent]: evento que notifica a la UI que debe navegar a la pantalla de login.
 *
 * @property getAllUsers Caso de uso para recuperar todos los usuarios.
 * @property deleteUser Caso de uso para eliminar un usuario dado su ID.
 * @property logoutUseCase Caso de uso para cerrar la sesión actual.
 */
@HiltViewModel
class UsersViewModel @Inject constructor(
    private val getAllUsers: GetAllUsersUseCase,
    private val deleteUser: DeleteUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    /**
     * LiveData que contiene la lista de usuarios recuperada del servidor.
     * - Se inicializa como null hasta que se llame a [loadUsers].
     * - La UI debe observar este LiveData para mostrar la lista en un RecyclerView u otro componente.
     */
    private val _users = MutableLiveData<List<UserDetailResponse>?>()
    val users: MutableLiveData<List<UserDetailResponse>?> = _users

    /**
     * LiveData que notifica un evento de logout.
     * - Se establece a true cuando se llama a [logout], indicando a la UI que debe navegar a login.
     * - Después de manejarse, la UI debe llamar a [resetLogoutEvent] para limpiar el evento.
     */
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    init {
        // Al inicializar el ViewModel, se carga automáticamente la lista de usuarios.
        loadUsers()
    }

    /**
     * Carga la lista completa de usuarios del sistema.
     *
     * - Ejecuta [getAllUsers] en una corrutina de [viewModelScope].
     * - Publica el resultado (lista de [UserDetailResponse]) en [_users].
     */
    fun loadUsers() = viewModelScope.launch {
        _users.value = getAllUsers()
    }

    /**
     * Elimina el usuario con el ID especificado.
     *
     * - Llama a [deleteUser] con el ID proporcionado.
     * - Si la operación retorna true (éxito), vuelve a cargar la lista de usuarios llamando a [loadUsers].
     *
     * @param id Identificador del usuario a eliminar.
     */
    fun removeUser(id: Int) = viewModelScope.launch {
        if (deleteUser(id)) loadUsers()
    }

    /**
     * Cierra la sesión del usuario actual.
     *
     * - Ejecuta [logoutUseCase] para limpiar los datos de sesión.
     * - Publica `true` en [_logoutEvent], indicando a la UI que debe navegar a la pantalla de login.
     */
    fun logout() {
        logoutUseCase()
        _logoutEvent.value = true
    }

    /**
     * Reinicia el evento de logout estableciendo [_logoutEvent] a false.
     *
     * - Debe llamarse desde la UI después de haber manejado el evento de logout para evitar navegaciones repetidas.
     */
    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }
}
