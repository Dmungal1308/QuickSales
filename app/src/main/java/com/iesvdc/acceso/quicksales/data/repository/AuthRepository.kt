package com.iesvdc.acceso.quicksales.data.repository

import android.content.Context
import android.util.Log
import com.iesvdc.acceso.quicksales.data.datasource.network.AuthApi
import com.iesvdc.acceso.quicksales.data.datasource.network.models.LoginRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.LoginResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.RegisterRequest
import com.iesvdc.acceso.quicksales.domain.models.UserData
import com.iesvdc.acceso.quicksales.domain.usercase.LoginResult
import com.iesvdc.acceso.quicksales.domain.usercase.RegistrationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.math.BigDecimal
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context
) {

    suspend fun login(
        correo: String,
        contrasena: String
    ): LoginResult {
        Log.d("AuthRepository", "login called with correo: $correo")
        return try {
            val response: LoginResponse = authApi.login(
                LoginRequest(correo = correo, contrasena = contrasena)
            )
            val token = response.token
            val userNet = response.user

            if (token.isBlank()) {
                Log.e("AuthRepository", "Empty token in response")
                return LoginResult.Error("Credenciales inválidas")
            }

            // Guardar token y userId
            val prefs = context.getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("jwt_token", token)
                putInt("user_id", userNet.id)
                apply()
            }
            Log.d("AuthRepository", "Token and userId saved: ${userNet.id}")

            // Mapear a UserData de dominio
            val userData = UserData(
                id            = userNet.id,
                nombre        = userNet.nombre,
                nombreUsuario = userNet.nombreUsuario,
                contrasena    = userNet.contrasena,
                correo        = userNet.correo,
                imagenBase64  = userNet.imagenBase64,
                rol           = userNet.rol,
                saldo         = BigDecimal(userNet.saldo)
            )
            LoginResult.Success(userData)

        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception in login", e)
            LoginResult.Error("Error en el login: ${e.message}")
        }
    }

    suspend fun register(
        nombre: String,
        nombreUsuario: String,
        contrasena: String,
        correo: String,
        imagenBase64: String? = null,
        rol: String? = "usuario"
    ): RegistrationResult {
        val req = RegisterRequest(
            nombre        = nombre,
            nombreUsuario = nombreUsuario,
            contrasena    = contrasena,
            correo        = correo,
            imagenBase64  = null,
            rol           = "usuario"
        )

        return try {
            // Si la llamada devuelve 2xx, supone éxito
            authApi.register(req)
            RegistrationResult.Success
        } catch (e: HttpException) {
            // Extraer el JSON de error: {"error":"mensaje"}
            val errorBody = e.response()?.errorBody()?.string()
            val msg = try {
                Json.decodeFromString<Map<String, String>>(errorBody ?: "")
                    .getOrDefault("error", "Error en el registro")
            } catch (_: Exception) {
                "Error en el registro: código ${e.code()}"
            }
            Log.e("AuthRepository", "Registro fallido: $msg")
            RegistrationResult.Error(msg)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Exception in register", e)
            RegistrationResult.Error("Error en el registro: ${e.localizedMessage}")
        }
    }
}
