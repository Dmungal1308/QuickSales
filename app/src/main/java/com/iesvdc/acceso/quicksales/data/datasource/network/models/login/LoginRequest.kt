package com.iesvdc.acceso.quicksales.data.datasource.network.models.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val correo: String,
    val contrasena: String
)
