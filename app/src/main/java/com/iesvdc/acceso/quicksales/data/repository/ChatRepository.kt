package com.iesvdc.acceso.quicksales.data.repository

import com.iesvdc.acceso.quicksales.data.datasource.network.ChatApi
import com.iesvdc.acceso.quicksales.data.datasource.network.CrearSesionRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.EnviarMensajeRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatMessageResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatSessionResponse
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
class ChatRepository @Inject constructor(
    private val api: ChatApi
) {
    suspend fun crearORecuperarSesion(prodId: Int, vendId: Int, compId: Int): ChatSessionResponse =
        api.crearSesion(CrearSesionRequest(prodId, vendId, compId))

    suspend fun obtenerMensajes(sesionId: Int): List<ChatMessageResponse> =
        api.obtenerMensajes(sesionId)

    suspend fun enviarMensaje(sesionId: Int, texto: String): ChatMessageResponse =
        api.enviarMensaje(sesionId, EnviarMensajeRequest(texto))

    suspend fun obtenerSesiones(): List<ChatSessionResponse> =
        api.getChatSessions()
}