package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.productos.comprados.GetPurchasedProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar la lógica relacionada con los productos comprados por el usuario.
 *
 * Utiliza el caso de uso [GetPurchasedProductsUseCase] para obtener la lista de productos
 * que el usuario ha adquirido. Expone LiveData para notificar a la UI sobre cambios en la lista
 * de productos comprados y eventos de logout.
 *
 * @param application Contexto de aplicación, necesario para heredar de AndroidViewModel.
 * @param getPurchasedProductsUseCase Caso de uso para recuperar los productos comprados.
 */
@HiltViewModel
class PurchasedProductsViewModel @Inject constructor(
    application: Application,
    private val getPurchasedProductsUseCase: GetPurchasedProductsUseCase
) : AndroidViewModel(application) {

    /**
     * LiveData que notifica un evento de logout. Puede usarse para retornar a pantalla de login
     * cuando sea necesario.
     */
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    /**
     * LiveData que contiene la lista de productos comprados por el usuario.
     * Se actualiza al llamar a [loadPurchased].
     */
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    init {
        // Al inicializar el ViewModel, cargamos los productos comprados.
        loadPurchased()
    }

    /**
     * Carga la lista de productos comprados por el usuario.
     *
     * - Llama a [getPurchasedProductsUseCase] para obtener la lista.
     * - Publica la lista en [_products].
     */
    fun loadPurchased() = viewModelScope.launch {
        _products.value = getPurchasedProductsUseCase()!!
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
