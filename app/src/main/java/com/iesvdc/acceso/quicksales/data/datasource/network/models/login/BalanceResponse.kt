package com.iesvdc.acceso.quicksales.data.datasource.network.models.login

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class BalanceResponse(
    val saldo: BigDecimal
)

@Serializable
data class OperationResponse(
    val success: Boolean,
    val message: String? = null
)