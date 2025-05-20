package com.iesvdc.acceso.quicksales.data.repository

import android.content.Context
import com.iesvdc.acceso.quicksales.data.datasource.network.UserApi
import com.iesvdc.acceso.quicksales.data.datasource.network.models.AmountRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.login.BalanceResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.ChangePasswordRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.login.OperationResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UpdateProfileRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserDetailResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    @ApplicationContext private val context: Context
){
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

    suspend fun getProfile(): UserResponse =
        userApi.getProfile()

    suspend fun getUserById(id: Int): UserResponse =
        userApi.getUserById(id)



    suspend fun updateProfile(
        nombre: String,
        nombreUsuario: String,
        correo: String,
        imagenBase64: String? = null
    ): UserResponse = userApi.updateProfile(
        UpdateProfileRequest(nombre, nombreUsuario, correo, imagenBase64)
    )


    suspend fun changePassword(newPassword: String) {
        userApi.changePassword(ChangePasswordRequest(newPassword))
    }

    suspend fun getAllUsers(): List<UserDetailResponse> =
        userApi.getAllUsers()

    suspend fun deleteUser(id: Int): Boolean {
        val resp = userApi.deleteUser(id)
        return resp.success ?: false
    }

}