package com.iesvdc.acceso.quicksales.domain.usercase.usuarios

import com.iesvdc.acceso.quicksales.data.datasource.network.UserApi
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserDetailResponse
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(private val api: UserApi) {
    suspend operator fun invoke(): List<UserDetailResponse> = api.getAllUsers()
}