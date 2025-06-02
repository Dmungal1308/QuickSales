package com.iesvdc.acceso.quicksales.data.repository

import android.content.Context
import android.util.Log
import com.iesvdc.acceso.quicksales.data.datasource.network.AuthApi
import com.iesvdc.acceso.quicksales.data.datasource.network.models.login.LoginRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.login.LoginResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.login.RegisterRequest
import com.iesvdc.acceso.quicksales.domain.models.UserData
import com.iesvdc.acceso.quicksales.domain.usercase.login.LoginResult
import com.iesvdc.acceso.quicksales.domain.usercase.login.RegistrationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Repositorio responsable de gestionar la autenticación de usuarios mediante llamadas
 * a la API remota. Proporciona funciones para iniciar sesión y registrar un nuevo usuario.
 *
 * @property authApi Cliente de red para interactuar con el servicio de autenticación.
 * @property context Contexto de la aplicación, utilizado para acceder a SharedPreferences.
 */
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    @ApplicationContext private val context: Context
) {

    /**
     * Intenta autenticar al usuario con las credenciales proporcionadas.
     * En caso de éxito, almacena el token JWT y el ID de usuario en SharedPreferences
     * y retorna un [LoginResult.Success] con los datos del usuario.
     * Si ocurre un error en la llamada o las credenciales son inválidas, retorna [LoginResult.Error].
     *
     * @param correo Dirección de correo electrónico del usuario.
     * @param contrasena Contraseña asociada a la cuenta del usuario.
     * @return [LoginResult.Success] con los datos del usuario si la autenticación es correcta,
     *         o [LoginResult.Error] con un mensaje descriptivo en caso contrario.
     */
    suspend fun login(
        correo: String,
        contrasena: String
    ): LoginResult {
        Log.d("AuthRepository", "login called with correo: $correo")
        return try {
            // Construir la petición de inicio de sesión
            val response: LoginResponse = authApi.login(
                LoginRequest(correo = correo, contrasena = contrasena)
            )

            val token = response.token
            val userNet = response.user

            // Validar que el token no venga vacío
            if (token.isBlank()) {
                Log.e("AuthRepository", "Empty token in response")
                return LoginResult.Error("Credenciales inválidas")
            }

            // Guardar token y userId en SharedPreferences para sesiones posteriores
            val prefs = context.getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString("jwt_token", token)
                putInt("user_id", userNet.id)
                apply()
            }
            Log.d("AuthRepository", "Token and userId saved: ${userNet.id}")

            // Mapear la respuesta de red a un modelo de dominio
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
            // Capturar cualquier excepción (Network, parsing, etc.) y devolver error
            Log.e("AuthRepository", "Exception in login", e)
            LoginResult.Error("Error en el login: ${e.message}")
        }
    }

    /**
     * Intenta registrar un nuevo usuario en el sistema.
     * Construye un [RegisterRequest] con los datos proporcionados y realiza la llamada a la API.
     * Si el registro se realiza con éxito, retorna [RegistrationResult.Success].
     * En caso de fallo (por ejemplo, usuario ya existente o error en el servidor), retorna [RegistrationResult.Error].
     *
     * @param nombre Nombre completo del nuevo usuario.
     * @param nombreUsuario Nombre de usuario único para el nuevo usuario.
     * @param contrasena Contraseña para la nueva cuenta.
     * @param correo Correo electrónico del nuevo usuario.
     * @param imagenBase64 (Opcional) Imagen de perfil en Base64.
     * @param rol (Opcional) Rol que se asignará al usuario; por defecto "usuario".
     * @return [RegistrationResult.Success] si el registro es exitoso,
     *         o [RegistrationResult.Error] con un mensaje descriptivo en caso contrario.
     */
    suspend fun register(
        nombre: String,
        nombreUsuario: String,
        contrasena: String,
        correo: String,
        imagenBase64: String? = null,
        rol: String? = "usuario"
    ): RegistrationResult {
        // Preparar el objeto de solicitud
        val req = RegisterRequest(
            nombre        = nombre,
            nombreUsuario = nombreUsuario,
            contrasena    = contrasena,
            correo        = correo,
            imagenBase64  = null,
            rol           = "usuario"
        )

        return try {
            // Llamada a la API para registrar al usuario
            authApi.register(req)
            RegistrationResult.Success

        } catch (e: HttpException) {
            // Si la respuesta HTTP tiene código de error, intentar decodificar el cuerpo
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
            // Capturar cualquier otra excepción y devolver mensaje genérico
            Log.e("AuthRepository", "Exception in register", e)
            RegistrationResult.Error("Error en el registro: ${e.localizedMessage}")
        }
    }
}
