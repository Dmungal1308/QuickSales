package com.iesvdc.acceso.quicksales.data.datasource.network.models.productos

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteResponse(
    val id: Int,
    val idUsuario: Int,
    val idProducto: Int,
    val fechaAgregado: String
)
