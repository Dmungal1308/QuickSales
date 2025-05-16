// File: com/iesvdc/acceso/quicksales/ui/modelview/FavoritosViewModel.kt
package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.AddFavoriteUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.GetFavoritesUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.GetOtherProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.PurchaseProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class FavoritosViewModel @Inject constructor(
    application: Application,
    private val getOtherProductsUseCase: GetOtherProductsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val logoutUseCase: LogoutUseCase
) : AndroidViewModel(application) {

    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    private val _favoriteIds = MutableLiveData<Set<Int>>(emptySet())
    val favoriteIds: LiveData<Set<Int>> = _favoriteIds

    private var allFavorites: List<ProductResponse> = emptyList()
    private val _purchaseError = MutableLiveData<String?>()
    val purchaseError: LiveData<String?> = _purchaseError

    fun purchaseById(id: Int) = viewModelScope.launch {
        try {
            purchaseProductUseCase(id)
            loadFavorites()
        } catch (e: Exception) {
            val msg = if (e is HttpException) {
                // leemos errorBody
                val errorBody = e.response()?.errorBody()?.string()
                // si es JSON con campo "error", lo aprovechamos
                val parsed = try {
                    val json = JSONObject(errorBody ?: "")
                    json.optString("error", "")
                } catch (_: Exception) { "" }
                if (parsed.contains("Saldo insuficiente", ignoreCase = true))
                    "No tienes saldo suficiente"
                else
                    "Error al comprar (${e.code()})"
            } else {
                e.message ?: "Error al comprar"
            }
            _purchaseError.value = msg
        }
    }
    fun clearPurchaseError() { _purchaseError.value = null }

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            // 1) obtengo todos los productos ajenos
            val all = getOtherProductsUseCase()
            // 2) obtengo sólo los IDs de favoritos
            val favIds = getFavoritesUseCase().map { it.idProducto }.toSet()
            val favList = all
                .filter { it.id in favIds }
                .filter { it.idComprador == null }
            _products.value = favList
            allFavorites = favList
            // **3) inicializo aquí también el LiveData de favoritos**
            _favoriteIds.value = favIds
            // 4) filtro la lista original por esos IDs
            _products.value = all.filter { it.id in favIds }
            allFavorites = all.filter { it.id in favIds }
            _products.value = allFavorites
        }
    }

    fun toggleFavorite(product: ProductResponse) {
        viewModelScope.launch {
            val current = _favoriteIds.value.orEmpty().toMutableSet()
            if (current.remove(product.id)) {
                removeFavoriteUseCase(product.id)
            } else {
                addFavoriteUseCase(product.id)
                current.add(product.id)
            }
            _favoriteIds.value = current
            // y si quieres, vuelves a refrescar sólo los favoritos:
            _products.value = _products.value?.filter { it.id in current }
        }
    }

    fun filterByName(query: String) {
        val q = Normalizer
            .normalize(query.trim(), Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
            .lowercase()

        _products.value = if (q.isEmpty()) {
            allFavorites
        } else {
            allFavorites.filter {
                it.nombre.lowercase().contains(q)
            }
        }
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
