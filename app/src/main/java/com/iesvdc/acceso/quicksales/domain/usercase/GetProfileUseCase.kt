package com.iesvdc.acceso.quicksales.domain.usercase

import com.iesvdc.acceso.quicksales.data.datasource.network.models.UserResponse
import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(): UserResponse =
        repo.getProfile()
}