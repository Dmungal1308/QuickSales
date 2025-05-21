package com.iesvdc.acceso.quicksales.data.datasource.network.models.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatSessionResponse(
    val idSesion: Int,
    val idProducto: Int,
    val idVendedor: Int,
    val idComprador: Int,
    val fechaCreacion: String = ""
)