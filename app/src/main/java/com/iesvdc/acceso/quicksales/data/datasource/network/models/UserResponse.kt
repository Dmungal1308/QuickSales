package com.iesvdc.acceso.quicksales.data.datasource.network.models

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Int,
    val nombre: String,
    val nombreUsuario: String,
    val correo: String,
    val imagenBase64: String?
)
