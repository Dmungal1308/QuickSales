package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatMessageResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatSessionResponse
import com.iesvdc.acceso.quicksales.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica de chat: iniciar o recuperar sesiones,
 * cargar mensajes y enviar nuevos mensajes.
 *
 * @property repo Instancia de [ChatRepository] para realizar las llamadas a la API de chat.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {

    /**
     * LiveData que expone la sesión de chat actual ([ChatSessionResponse]) tras iniciarla
     * o recuperarla.
     */
    private val _sesion = MutableLiveData<ChatSessionResponse>()
    val sesion: LiveData<ChatSessionResponse> = _sesion

    /**
     * LiveData que contiene la lista de mensajes ([ChatMessageResponse]) de la sesión actual.
     * Se inicializa con una lista vacía.
     */
    private val _mensajes = MutableLiveData<List<ChatMessageResponse>>(emptyList())
    val mensajes: LiveData<List<ChatMessageResponse>> = _mensajes

    /**
     * LiveData que contiene el mensaje de error en caso de fallo en alguna operación de chat.
     */
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /**
     * Inicia o recupera una sesión de chat según los IDs de producto, vendedor y comprador.
     *
     * - Llama a [repo.crearORecuperarSesion] con los parámetros recibidos.
     * - Si la llamada es exitosa, publica la sesión en [_sesion] y llama a [cargarMensajes].
     * - Si ocurre una excepción, publica un mensaje de error en [_error].
     *
     * @param prodId ID del producto para la sesión de chat.
     * @param vendId ID del vendedor asociado al chat.
     * @param compId ID del comprador que inicia o recupera la sesión.
     */
    fun iniciarSesion(prodId: Int, vendId: Int, compId: Int) = viewModelScope.launch {
        try {
            val s = repo.crearORecuperarSesion(prodId, vendId, compId)
            _sesion.value = s
            cargarMensajes()
        } catch (e: Exception) {
            _error.value = "No se pudo iniciar chat: ${e.localizedMessage}"
        }
    }

    /**
     * Establece manualmente una sesión de chat a partir de su ID de sesión.
     *
     * - Crea un objeto [ChatSessionResponse] con solo el ID de sesión (los otros campos no se usan)
     *   y lo publica en [_sesion].
     * - Luego llama a [cargarMensajes] para obtener los mensajes de esa sesión.
     *
     * @param sessionId ID de la sesión de chat a establecer.
     */
    fun iniciarSesionSessionId(sessionId: Int) {
        _sesion.value = ChatSessionResponse(
            idSesion    = sessionId,
            idProducto  = 0,
            idVendedor  = 0,
            idComprador = 0
        )
        cargarMensajes()
    }

    /**
     * Carga los mensajes de la sesión de chat actual.
     *
     * - Comprueba si [_sesion] tiene un valor; si es así, llama a [repo.obtenerMensajes]
     *   usando el ID de sesión.
     * - Si la llamada es exitosa, concatena la lista obtenida en [_mensajes].
     * - Si ocurre una excepción, publica un mensaje de error en [_error].
     */
    fun cargarMensajes() = viewModelScope.launch {
        sesion.value?.let { s ->
            try {
                val list = repo.obtenerMensajes(s.idSesion)
                _mensajes.value = list
            } catch (e: Exception) {
                _error.value = "Error al cargar mensajes"
            }
        }
    }

    /**
     * Envía un nuevo mensaje de texto a la sesión de chat actual.
     *
     * - Obtiene el ID de la sesión desde [sesion]; si no existe, sale de la función.
     * - Llama a [repo.enviarMensaje] con el ID de sesión y el texto recibido.
     * - Si la llamada es exitosa, agrega el mensaje devuelto a la lista [_mensajes].
     * - Si ocurre una excepción, publica un mensaje de error en [_error].
     *
     * @param texto Texto del mensaje que se desea enviar.
     */
    fun enviarMensaje(texto: String) = viewModelScope.launch {
        val sesionId = sesion.value?.idSesion ?: return@launch
        try {
            val msg = repo.enviarMensaje(sesionId, texto)
            _mensajes.value = _mensajes.value.orEmpty() + msg
        } catch (e: Exception) {
            _error.value = "No se pudo enviar mensaje"
        }
    }
}
