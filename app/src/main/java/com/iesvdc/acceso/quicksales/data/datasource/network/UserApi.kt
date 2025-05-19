package com.iesvdc.acceso.quicksales.data.datasource.network

import com.iesvdc.acceso.quicksales.data.datasource.network.models.login.BalanceResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.login.OperationResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.AmountRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.ChangePasswordRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UpdateProfileRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
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
    @GET("users/me")
    suspend fun getProfile(): UserResponse

    @PUT("users/me")
    suspend fun updateProfile(@Body req: UpdateProfileRequest): UserResponse

    @PUT("users/me/password")
    suspend fun changePassword(@Body req: ChangePasswordRequest)

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Int): UserResponse


}
