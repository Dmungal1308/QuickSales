package com.iesvdc.acceso.quicksales.data.repository

import android.content.Context
import com.iesvdc.acceso.quicksales.data.datasource.network.UserApi
import com.iesvdc.acceso.quicksales.data.datasource.network.models.AmountRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.BalanceResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.OperationResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    @ApplicationContext private val context: Context
){
    // Recuperar userId de SharedPreferences (se guardó al hacer login)
    private fun getUserId(): Int {
        val prefs = context.getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
        return prefs.getInt("user_id", -1)
    }

    suspend fun getBalance(): BalanceResponse {
        return userApi.getBalance(getUserId())
    }

    suspend fun deposit(amount: BigDecimal): OperationResponse {
            return userApi.deposit(getUserId(), AmountRequest(cantidad = amount))
    }

    suspend fun withdraw(amount: BigDecimal): OperationResponse {
            return userApi.withdraw(getUserId(), AmountRequest(cantidad = amount))
    }
}