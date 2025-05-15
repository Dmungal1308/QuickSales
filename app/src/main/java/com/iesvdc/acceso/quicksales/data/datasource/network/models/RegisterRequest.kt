package com.iesvdc.acceso.quicksales.data.datasource.network.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val nombre: String,
    val nombreUsuario: String,
    val contrasena: String,
    val correo: String,
    val imagenBase64: String? = null,
    val rol: String? = null
)
