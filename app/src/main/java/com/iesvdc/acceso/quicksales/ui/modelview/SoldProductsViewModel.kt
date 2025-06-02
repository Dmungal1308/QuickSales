package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.productos.vendidos.GetSoldProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica relacionada con los productos vendidos por el usuario.
 *
 * Utiliza el caso de uso [GetSoldProductsUseCase] para obtener la lista de productos
 * que el usuario ha vendido. Expone LiveData para notificar a la UI sobre cambios en la lista
 * de productos vendidos y para manejar eventos de logout.
 *
 * @param application Contexto de la aplicación, necesario para heredar de AndroidViewModel.
 * @param getSoldProductsUseCase Caso de uso para recuperar los productos vendidos.
 */
@HiltViewModel
class SoldProductsViewModel @Inject constructor(
    application: Application,
    private val getSoldProductsUseCase: GetSoldProductsUseCase
) : AndroidViewModel(application) {

    /**
     * LiveData que notifica un evento de logout.
     * Puede usarse para retornar a la pantalla de login cuando sea necesario.
     */
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    /**
     * LiveData que contiene la lista de productos vendidos por el usuario.
     * Se actualiza al llamar a [loadPurchased].
     */
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    init {
        // Al inicializar el ViewModel, cargamos los productos vendidos.
        loadPurchased()
    }

    /**
     * Carga la lista de productos vendidos por el usuario.
     *
     * - Llama a [getSoldProductsUseCase] en una corrutina de [viewModelScope].
     * - Publica la lista obtenida en [_products].
     */
    fun loadPurchased() = viewModelScope.launch {
        _products.value = getSoldProductsUseCase()!!
    }

    /**
     * Reinicia el evento de logout estableciendo [_logoutEvent] a false.
     *
     * Útil para limpiar el estado después de manejar el evento en la interfaz de usuario.
     */
    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }
}
