package com.iesvdc.acceso.quicksales.data.datasource.network

import com.iesvdc.acceso.quicksales.data.datasource.network.models.LoginRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.LoginResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.RegisterRequest

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
