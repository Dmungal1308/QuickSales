package com.iesvdc.acceso.quicksales.domain.usercase.usuarios

import com.iesvdc.acceso.quicksales.data.datasource.network.UserApi
import javax.inject.Inject

class DeleteUserUseCase @Inject constructor(private val api: UserApi) {
    suspend operator fun invoke(id: Int): Boolean {
        return api.deleteUser(id).success
    }
}