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

/**
 * Repositorio encargado de manejar las operaciones relacionadas con el usuario,
 * tales como obtener saldo, depositar, retirar, obtener y actualizar perfil,
 * y cambiar contraseña. Utiliza [UserApi] para realizar llamadas a la API remota.
 *
 * @property userApi Cliente Retrofit para interactuar con el servicio de usuarios.
 * @property context Contexto de la aplicación, utilizado para acceder a SharedPreferences.
 */
@Singleton
class UserRepository @Inject constructor(
    private val userApi: UserApi,
    @ApplicationContext private val context: Context
) {
    /**
     * Recupera el ID de usuario almacenado en SharedPreferences ("user_id").
     *
     * @return ID del usuario actual, o -1 si no está presente.
     */
    private fun getUserId(): Int {
        val prefs = context.getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
        return prefs.getInt("user_id", -1)
    }

    /**
     * Obtiene el balance actual del usuario.
     *
     * Llama a la API enviando el ID del usuario y retorna la respuesta de tipo [BalanceResponse].
     *
     * @return [BalanceResponse] con la información del saldo del usuario.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun getBalance(): BalanceResponse {
        return userApi.getBalance(getUserId())
    }

    /**
     * Realiza un depósito en la cuenta del usuario.
     *
     * Envía la cantidad especificada como [AmountRequest] a la API para incrementar el saldo.
     *
     * @param amount Cantidad a depositar en formato [BigDecimal].
     * @return [OperationResponse] con el resultado de la operación.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun deposit(amount: BigDecimal): OperationResponse {
        return userApi.deposit(getUserId(), AmountRequest(cantidad = amount))
    }

    /**
     * Realiza un retiro de la cuenta del usuario.
     *
     * Envía la cantidad especificada como [AmountRequest] a la API para decrementar el saldo.
     *
     * @param amount Cantidad a retirar en formato [BigDecimal].
     * @return [OperationResponse] con el resultado de la operación.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun withdraw(amount: BigDecimal): OperationResponse {
        return userApi.withdraw(getUserId(), AmountRequest(cantidad = amount))
    }

    /**
     * Recupera el perfil del usuario actual.
     *
     * Llama a la API sin parámetros adicionales y retorna la información de perfil.
     *
     * @return [UserResponse] con los datos del perfil del usuario.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun getProfile(): UserResponse =
        userApi.getProfile()

    /**
     * Obtiene los detalles de un usuario por su ID.
     *
     * @param id Identificador del usuario a recuperar.
     * @return [UserResponse] con los datos del usuario especificado.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun getUserById(id: Int): UserResponse =
        userApi.getUserById(id)

    /**
     * Actualiza el perfil del usuario actual con los datos proporcionados.
     *
     * @param nombre Nombre completo del usuario.
     * @param nombreUsuario Nombre de usuario único.
     * @param correo Correo electrónico del usuario.
     * @param imagenBase64 (Opcional) Imagen de perfil en Base64.
     * @return [UserResponse] con los datos actualizados del perfil.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun updateProfile(
        nombre: String,
        nombreUsuario: String,
        correo: String,
        imagenBase64: String? = null
    ): UserResponse = userApi.updateProfile(
        UpdateProfileRequest(nombre, nombreUsuario, correo, imagenBase64)
    )

    /**
     * Cambia la contraseña del usuario actual.
     *
     * Envía la nueva contraseña como [ChangePasswordRequest] a la API.
     *
     * @param newPassword Nueva contraseña a establecer.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun changePassword(newPassword: String) {
        userApi.changePassword(ChangePasswordRequest(newPassword))
    }
}
