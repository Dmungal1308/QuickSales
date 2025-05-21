package com.iesvdc.acceso.quicksales.data.datasource.network

import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatMessageResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatSessionResponse
import retrofit2.http.*

interface ChatApi {
    @POST("chats/sesion")
    suspend fun crearSesion(@Body req: CrearSesionRequest): ChatSessionResponse

    @GET("chats/{sesionId}/mensajes")
    suspend fun obtenerMensajes(@Path("sesionId") sesionId: Int): List<ChatMessageResponse>

    @POST("chats/{sesionId}/mensajes")
    suspend fun enviarMensaje(
        @Path("sesionId") sesionId: Int,
        @Body req: EnviarMensajeRequest
    ): ChatMessageResponse

    @GET("chats")
    suspend fun getChatSessions(): List<ChatSessionResponse>
}

@kotlinx.serialization.Serializable
data class CrearSesionRequest(val idProducto: Int, val idVendedor: Int, val idComprador: Int)

@kotlinx.serialization.Serializable
data class EnviarMensajeRequest(val texto: String)