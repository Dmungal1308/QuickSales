package com.iesvdc.acceso.quicksales.domain.usercase.login

sealed class RegistrationResult {
    object Success : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
}