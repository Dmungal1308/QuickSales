package com.iesvdc.acceso.quicksales.domain.usercase.login

import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import javax.inject.Inject

class ChangePasswordUseCase @Inject constructor(
    private val repo: UserRepository
) {

    suspend operator fun invoke(newPassword: String) {
        repo.changePassword(newPassword)
    }
}