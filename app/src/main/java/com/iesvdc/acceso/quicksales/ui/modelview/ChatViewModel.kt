package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatMessageResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatSessionResponse
import com.iesvdc.acceso.quicksales.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {

    private val _sesion = MutableLiveData<ChatSessionResponse>()
    val sesion: LiveData<ChatSessionResponse> = _sesion

    private val _mensajes = MutableLiveData<List<ChatMessageResponse>>(emptyList())
    val mensajes: LiveData<List<ChatMessageResponse>> = _mensajes

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun iniciarSesion(prodId: Int, vendId: Int, compId: Int) = viewModelScope.launch {
        try {
            val s = repo.crearORecuperarSesion(prodId, vendId, compId)
            _sesion.value = s
            cargarMensajes()
        } catch (e: Exception) {
            _error.value = "No se pudo iniciar chat: ${e.localizedMessage}"
        }
    }

    fun iniciarSesionSessionId(sessionId: Int) {
        _sesion.value = ChatSessionResponse(
            idSesion    = sessionId,
            idProducto  = 0,
            idVendedor  = 0,
            idComprador = 0
        )
        cargarMensajes()
    }


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

    fun enviarMensaje(texto: String) = viewModelScope.launch {
        val sesionId = sesion.value?.idSesion ?: return@launch
        try {
            val msg = repo.enviarMensaje(sesionId, texto)
            // Añadir al final de la lista
            _mensajes.value = _mensajes.value.orEmpty() + msg
        } catch (e: Exception) {
            _error.value = "No se pudo enviar mensaje"
        }
    }
}