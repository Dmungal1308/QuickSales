package com.iesvdc.acceso.quicksales.domain.usercase

sealed class RegistrationResult {
    object Success : RegistrationResult()
    data class Error(val message: String) : RegistrationResult()
}