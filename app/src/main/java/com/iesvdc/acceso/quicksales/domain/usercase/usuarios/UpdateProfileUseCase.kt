package com.iesvdc.acceso.quicksales.domain.usercase.usuarios

import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserResponse
import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repo: UserRepository
) {

    suspend operator fun invoke(
        nombre: String,
        nombreUsuario: String,
        correo: String,
        imagenBase64: String?
    ): UserResponse =
        repo.updateProfile(nombre, nombreUsuario, correo, imagenBase64)
}