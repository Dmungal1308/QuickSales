// File: com/iesvdc/acceso/quicksales/ui/modelview/MenuViewModel.kt
package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.GetOtherProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.PurchaseProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    application: Application,
    private val getOtherProductsUseCase: GetOtherProductsUseCase,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val logoutUseCase: LogoutUseCase
) : AndroidViewModel(application) {

    // Lista completa sin filtrar
    private val allProducts = mutableListOf<ProductResponse>()

    // LiveData que expone la lista filtrada
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    // LiveData para logout
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    init {
        loadOthers()
    }

    /** Carga todos los productos ajenos y resetea filtro */
    fun loadOthers() {
        viewModelScope.launch {
            val list = getOtherProductsUseCase()
            allProducts.clear()
            allProducts += list
            _products.value = list
        }
    }

    /**
     * Filtra por nombre:
     * - Ignora mayúsculas/minúsculas
     * - Permite buscar con fragmentos
     * - Elimina acentos
     */
    fun filterByName(query: String) {
        val q = normalize(query.trim())
        _products.value = if (q.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { normalize(it.nombre).contains(q) }
        }
    }

    fun purchase(product: ProductResponse) {
        viewModelScope.launch {
            purchaseProductUseCase(product.id)
            loadOthers()
        }
    }

    fun logout() {
        logoutUseCase()
        _logoutEvent.value = true
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

    /** Normaliza texto: descompone acentos y pasa a minúsculas */
    private fun normalize(text: String): String {
        val temp = Normalizer.normalize(text, Normalizer.Form.NFD)
        return temp.replace("\\p{M}".toRegex(), "").lowercase()
    }
}
