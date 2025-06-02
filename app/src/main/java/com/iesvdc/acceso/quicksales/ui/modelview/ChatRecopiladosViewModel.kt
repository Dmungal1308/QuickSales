package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatSessionResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.chat.GetChatSessionsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.GetProductByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de recuperar las sesiones de chat y asociar cada sesión con su producto correspondiente.
 *
 * Utiliza los casos de uso [GetChatSessionsUseCase] para obtener las sesiones y [GetProductByIdUseCase]
 * para solicitar los datos del producto asociado a cada sesión.
 *
 * @property getChatSessions Caso de uso para obtener todas las sesiones de chat del usuario.
 * @property getProductById Caso de uso para obtener la información detallada de un producto por su ID.
 */
@HiltViewModel
class ChatRecopiladosViewModel @Inject constructor(
    private val getChatSessions: GetChatSessionsUseCase,
    private val getProductById: GetProductByIdUseCase,
) : ViewModel() {

    /**
     * Data class que representa un elemento en la lista de sesiones de chat con su producto.
     *
     * @property session Objeto [ChatSessionResponse] que contiene la información de la sesión de chat.
     * @property product Objeto [ProductResponse] con los datos del producto asociado a la sesión.
     */
    data class Item(
        val session: ChatSessionResponse,
        val product: ProductResponse
    )

    /**
     * LiveData que expone la lista de elementos (sesión + producto) cargados.
     */
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items

    /**
     * LiveData que notifica un evento de logout (se manea fuera de este ViewModel).
     */
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    /**
     * Lanza la carga de sesiones de chat.
     *
     * - Obtiene la lista de sesiones llamando a [getChatSessions].
     * - Para cada sesión, recupera el producto asociado mediante [getProductById].
     * - Empaqueta cada par (sesión, producto) en un [Item] y publica la lista en [_items].
     */
    fun load() = viewModelScope.launch {
        val sessions = getChatSessions()
        val list = sessions.map { s ->
            val prod = getProductById(s.idProducto)
            Item(session = s, product = prod)
        }
        _items.value = list
    }

    /**
     * Reinicia el evento de logout, estableciendo su valor a false.
     *
     * Útil para limpiar el estado luego de manejar el evento en la interfaz de usuario.
     */
    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

}
