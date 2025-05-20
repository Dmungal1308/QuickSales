package com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios

import com.iesvdc.acceso.quicksales.domain.serializers.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class UserDetailResponse(
    val id: Int,
    val nombre: String,
    val nombreUsuario: String,
    val correo: String,
    val imagenBase64: String? = null,
    val rol: String,
    @Serializable(with = BigDecimalSerializer::class)
    val saldo: BigDecimal
)