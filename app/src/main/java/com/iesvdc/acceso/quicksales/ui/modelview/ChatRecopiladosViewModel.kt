package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.chat.ChatSessionResponse
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.chat.GetChatSessionsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.GetProductByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRecopiladosViewModel @Inject constructor(
    private val getChatSessions: GetChatSessionsUseCase,
    private val getProductById: GetProductByIdUseCase,
    private val logoutUseCase: LogoutUseCase

) : ViewModel() {

    data class Item(
        val session: ChatSessionResponse,
        val product: ProductResponse
    )

    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    fun load() = viewModelScope.launch {
        val sessions = getChatSessions()
        val list = sessions.map { s ->
            val prod = getProductById(s.idProducto)
            Item(session = s, product = prod)
        }
        _items.value = list
    }

    fun logout() {
        logoutUseCase()
        _logoutEvent.value = true
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }
    fun clearLogout() { _logoutEvent.value = false }
}

