package com.iesvdc.acceso.quicksales.data.repository

import com.iesvdc.acceso.quicksales.data.datasource.network.ChatApi
import com.iesvdc.acceso.quicksales.data.datasource.network.CrearSesionRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.EnviarMensajeRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatMessageResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatSessionResponse
import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

/**
 * Repositorio responsable de gestionar las operaciones relacionadas con el chat
 * a través de llamadas a la API remota. Permite crear o recuperar sesiones de chat,
 * obtener mensajes, enviar mensajes y listar todas las sesiones de chat.
 *
 * Se utiliza en el contexto de ViewModels u otros casos de uso donde se requiere
 * comunicarse con el servicio de chat.
 *
 * @property api Cliente Retrofit para interactuar con el servicio de chat remoto.
 */
@ActivityRetainedScoped
class ChatRepository @Inject constructor(
    private val api: ChatApi
) {
    /**
     * Crea una nueva sesión de chat o recupera una existente basándose en los identificadores
     * de producto, vendedor y comprador.
     *
     * Si ya existe una sesión activa con los mismos parámetros, el servidor devolverá
     * la sesión existente. De lo contrario, se creará una nueva sesión.
     *
     * @param prodId Identificador del producto sobre el que se inicia o recupera la sesión.
     * @param vendId Identificador del vendedor asociado al producto.
     * @param compId Identificador del comprador que inicia o recupera la sesión.
     * @return [ChatSessionResponse] que contiene información de la sesión (ID, participantes, etc.).
     * @throws Exception Si ocurre un error de red o la respuesta no es válida.
     */
    suspend fun crearORecuperarSesion(prodId: Int, vendId: Int, compId: Int): ChatSessionResponse =
        api.crearSesion(CrearSesionRequest(prodId, vendId, compId))

    /**
     * Obtiene la lista de mensajes asociados a una sesión de chat específica.
     *
     * @param sesionId Identificador de la sesión de chat de la que se desean recuperar los mensajes.
     * @return Lista de [ChatMessageResponse] con todos los mensajes de la sesión,
     *         incluyendo remitente, contenido y marca de tiempo.
     * @throws Exception Si ocurre un error de red o la respuesta no es válida.
     */
    suspend fun obtenerMensajes(sesionId: Int): List<ChatMessageResponse> =
        api.obtenerMensajes(sesionId)

    /**
     * Envía un nuevo mensaje de texto dentro de la sesión de chat especificada.
     *
     * @param sesionId Identificador de la sesión en la que se enviará el mensaje.
     * @param texto Contenido de texto del mensaje a enviar.
     * @return [ChatMessageResponse] que representa el mensaje recién enviado,
     *         incluyendo ID, remitente, contenido y fecha/hora.
     * @throws Exception Si ocurre un error de red o la respuesta no es válida.
     */
    suspend fun enviarMensaje(sesionId: Int, texto: String): ChatMessageResponse =
        api.enviarMensaje(sesionId, EnviarMensajeRequest(texto))

    /**
     * Recupera todas las sesiones de chat disponibles para el usuario o contexto actual.
     *
     * @return Lista de [ChatSessionResponse] con información de cada sesión de chat,
     *         incluyendo ID, participantes y estado de la sesión.
     * @throws Exception Si ocurre un error de red o la respuesta no es válida.
     */
    suspend fun obtenerSesiones(): List<ChatSessionResponse> =
        api.getChatSessions()
}
