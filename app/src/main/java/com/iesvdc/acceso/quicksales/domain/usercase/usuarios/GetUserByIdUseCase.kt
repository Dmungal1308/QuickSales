package com.iesvdc.acceso.quicksales.domain.usercase.usuarios

import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserResponse
import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import javax.inject.Inject

class GetUserByIdUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(userId: Int): UserResponse {
        return repo.getUserById(userId)
    }
}
