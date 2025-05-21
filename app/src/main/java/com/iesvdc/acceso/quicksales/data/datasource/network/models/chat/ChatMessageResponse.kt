package com.iesvdc.acceso.quicksales.data.datasource.network.models.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageResponse(
    val idMensaje: Int,
    val idSesion: Int,
    val idRemitente: Int,
    val texto: String,
    val fechaEnvio: String
)