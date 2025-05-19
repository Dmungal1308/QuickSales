package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.AddFavoriteUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.GetFavoritesUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.GetOtherProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.PurchaseProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.RemoveFavoriteUseCase
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
                val errorBody = e.response()?.errorBody()?.string()
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

    fun loadFavorites() {
        viewModelScope.launch {
            val all = getOtherProductsUseCase()
            val favIds = getFavoritesUseCase().map { it.idProducto }.toSet()
            val favList = all
                .filter { it.id in favIds }
                .filter { it.idComprador == null }
            _products.value = favList
            allFavorites = favList
            _favoriteIds.value = favIds
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
