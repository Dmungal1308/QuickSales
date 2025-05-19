package com.iesvdc.acceso.quicksales.data.datasource.network.models.productos

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ProductRequest(
    val nombre: String,
    val descripcion: String?,
    val imagenBase64: String?,
    val precio: BigDecimal
)