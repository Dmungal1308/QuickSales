package com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val nombre: String,
    val nombreUsuario: String,
    val correo: String,
    val imagenBase64: String? = null
)

@Serializable
data class ChangePasswordRequest(
    val newPassword: String
)