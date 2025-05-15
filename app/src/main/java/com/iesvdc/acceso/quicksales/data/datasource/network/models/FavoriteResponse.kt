package com.iesvdc.acceso.quicksales.data.datasource.network.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class FavoriteResponse(
    val id: Int,
    val idUsuario: Int,
    val idProducto: Int,
    val fechaAgregado: String
)
