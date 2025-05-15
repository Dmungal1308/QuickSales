package com.iesvdc.acceso.quicksales.data.datasource.network.models

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ProductResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val imagenBase64: String?,
    val precio: BigDecimal,
    val estado: String,
    val idVendedor: Int,
    val idComprador: Int?
)