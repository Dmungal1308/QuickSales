// File: com/iesvdc/acceso/quicksales/data/datasource/network/UserApi.kt

package com.iesvdc.acceso.quicksales.data.datasource.network

import com.iesvdc.acceso.quicksales.data.datasource.network.models.BalanceResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.OperationResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.AmountRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {
    @GET("users/{id}/saldo")
    suspend fun getBalance(@Path("id") userId: Int): BalanceResponse

    @POST("users/{id}/depositar")
    suspend fun deposit(
        @Path("id") userId: Int,
        @Body request: AmountRequest
    ): OperationResponse

    @POST("users/{id}/retirar")
    suspend fun withdraw(
        @Path("id") userId: Int,
        @Body request: AmountRequest
    ): OperationResponse
}
