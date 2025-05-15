package com.iesvdc.acceso.quicksales.domain.models

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import com.iesvdc.acceso.quicksales.domain.serializers.BigDecimalSerializer

/**
 * Modelo de dominio Usuario en la app Android, alineado con la API.
 */
@Serializable
data class UserData(
    val id: Int,
    val nombre: String,
    val nombreUsuario: String,
    val contrasena: String,
    val correo: String,
    val imagenBase64: String? = null,
    val rol: String? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val saldo: BigDecimal
)