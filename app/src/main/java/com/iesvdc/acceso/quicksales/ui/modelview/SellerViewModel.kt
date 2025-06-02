package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserResponse
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.GetUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica relacionada con la obtención
 * de información de un vendedor a partir de su ID.
 *
 * Utiliza el caso de uso [GetUserByIdUseCase] para solicitar los datos
 * del usuario (vendedor) y expone los resultados a la UI mediante LiveData.
 *
 * @property getUserByIdUseCase Caso de uso para recuperar los detalles de un usuario por ID.
 */
@HiltViewModel
class SellerViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModel() {

    /**
     * LiveData que contiene la información del usuario (vendedor) recuperado.
     *
     * - Se inicializa vacío y se actualiza cuando se invoca [loadUser].
     * - La UI puede observar este LiveData para mostrar los datos del vendedor.
     */
    private val _user = MutableLiveData<UserResponse>()
    val user: LiveData<UserResponse> = _user

    /**
     * Inicia la carga de la información de un usuario (vendedor) dado su [id].
     *
     * - Lanza una corrutina en [viewModelScope] para ejecutar [getUserByIdUseCase].
     * - Si la llamada es exitosa, publica el resultado en [_user].
     * - Si falla (retorna null o lanza excepción), no se actualiza el valor de [_user].
     *
     * @param id Identificador del usuario (vendedor) a recuperar.
     */
    fun loadUser(id: Int) = viewModelScope.launch {
        _user.value = getUserByIdUseCase(id)!!
    }
}
