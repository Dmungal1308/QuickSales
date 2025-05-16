// File: com/iesvdc/acceso/quicksales/ui/modelview/FavoritosViewModel.kt
package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.GetFavoritesUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.GetOtherProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.PurchaseProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class FavoritosViewModel @Inject constructor(
    application: Application,
    private val getOtherProductsUseCase: GetOtherProductsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val logoutUseCase: LogoutUseCase
) : AndroidViewModel(application) {

    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            // 1) obtengo todos los productos ajenos
            val all = getOtherProductsUseCase()
            // 2) obtengo sólo los IDs de favoritos
            val favIds = getFavoritesUseCase().map { it.idProducto }.toSet()
            // 3) filtro la lista original por esos IDs
            _products.value = all.filter { it.id in favIds }
        }
    }

    fun filterByName(query: String) {
        val q = Normalizer
            .normalize(query.trim(), Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
            .lowercase()
        _products.value = _products.value
            ?.filter { it.nombre.lowercase().contains(q) }
    }

    fun purchase(product: ProductResponse) {
        viewModelScope.launch {
            purchaseProductUseCase(product.id)
            loadFavorites()
        }
    }

    fun removeFavorite(product: ProductResponse) {
        viewModelScope.launch {
            removeFavoriteUseCase(product.id)
            loadFavorites()
        }
    }

    fun logout() {
        logoutUseCase()
        _logoutEvent.value = true
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }
}
