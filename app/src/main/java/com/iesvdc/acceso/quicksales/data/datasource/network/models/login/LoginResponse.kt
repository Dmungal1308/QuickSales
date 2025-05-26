package com.iesvdc.acceso.quicksales.data.datasource.network.models.login

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val token: String, val user: UserNetworkModel)

@Serializable
data class UserNetworkModel(
    val id: Int,
    val nombre: String,
    val nombreUsuario: String,
    val contrasena: String,
    val correo: String,
    val imagenBase64: String? = null,
    val rol: String? = null,
    val saldo: String
)

