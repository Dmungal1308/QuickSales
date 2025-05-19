package com.iesvdc.acceso.quicksales.domain.usercase.login


import javax.inject.Inject

sealed class ResetPasswordResult {
    object Success : ResetPasswordResult()
    data class Error(val message: String) : ResetPasswordResult()
}


class ResetPasswordUseCase @Inject constructor(
) {

}
