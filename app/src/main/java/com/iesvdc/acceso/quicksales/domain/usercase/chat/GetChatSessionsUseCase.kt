package com.iesvdc.acceso.quicksales.domain.usercase.chat

import com.iesvdc.acceso.quicksales.data.repository.ChatRepository
import javax.inject.Inject

class GetChatSessionsUseCase @Inject constructor(
    private val repo: ChatRepository
) {
    suspend operator fun invoke() = repo.obtenerSesiones()
}
