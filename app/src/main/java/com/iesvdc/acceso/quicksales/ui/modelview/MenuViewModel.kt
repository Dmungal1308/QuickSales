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
import retrofit2.HttpException
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    application: Application,
    private val getOtherProductsUseCase: GetOtherProductsUseCase,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase
) : AndroidViewModel(application) {

    private val allProducts = mutableListOf<ProductResponse>()
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    private val favoriteIds = mutableSetOf<Int>()
    private val _favoriteIdsLive = MutableLiveData<Set<Int>>()
    val favoriteIdsLive: LiveData<Set<Int>> = _favoriteIdsLive

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    private val _purchaseError   = MutableLiveData<String?>()
    val purchaseError: LiveData<String?> = _purchaseError

    private val _purchaseSuccess = MutableLiveData<Boolean>()
    val purchaseSuccess: LiveData<Boolean> = _purchaseSuccess

    fun purchaseById(productId: Int) {
        viewModelScope.launch {
            try {
                purchaseProductUseCase(productId)
                _purchaseSuccess.value = true
                loadData()
            } catch (e: HttpException) {
                if (e.code() == 400) _purchaseError.value = "No tienes saldo suficiente"
                else                  _purchaseError.value = e.message()
            } catch (e: Exception) {
                _purchaseError.value = e.message ?: "Error al comprar"
            }
        }
    }

    fun clearPurchaseError()   { _purchaseError.value = null }
    fun clearPurchaseSuccess() { _purchaseSuccess.value = false }
    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            val prods = getOtherProductsUseCase()
            val favs = getFavoritesUseCase().map { it.idProducto }
            favoriteIds.clear()
            favoriteIds.addAll(favs)
            _favoriteIdsLive.value = favoriteIds
            allProducts.clear()
            allProducts += prods
            _products.value = allProducts

        }
    }


    fun filterByName(query: String) {
        val q = Normalizer
            .normalize(query.trim(), Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "")
            .lowercase()
        _products.value = if (q.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { normalize(it.nombre).contains(q) }
        }
    }


    fun toggleFavorite(product: ProductResponse) {
        viewModelScope.launch {
            if (favoriteIds.contains(product.id)) {
                removeFavoriteUseCase(product.id)
                favoriteIds.remove(product.id)
            } else {
                addFavoriteUseCase(product.id)
                favoriteIds.add(product.id)
            }
            _favoriteIdsLive.value = favoriteIds
        }
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

    private fun normalize(text: String): String {
        val temp = Normalizer.normalize(text, Normalizer.Form.NFD)
        return temp.replace("\\p{M}".toRegex(), "").lowercase()
    }

}
