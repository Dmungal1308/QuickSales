package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.AddFavoriteUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.GetFavoritesUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.GetOtherProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.comprados.GetPurchasedProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.PurchaseProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class ProductosCompradosViewModel @Inject constructor(
    application: Application,
    private val getOtherProductsUseCase: GetOtherProductsUseCase,
    private val getPurchasedProductsUseCase: GetPurchasedProductsUseCase,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase,
    private val logoutUseCase: LogoutUseCase
) : AndroidViewModel(application) {



    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products
    init { loadPurchased() }
    fun loadPurchased() = viewModelScope.launch {
        _products.value = getPurchasedProductsUseCase()!!
    }

    fun logout() {
        logoutUseCase()
        _logoutEvent.value = true
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

    private fun normalize(text: String): String {
        val temp = Normalizer.normalize(text, Normalizer.Form.NFD)
        return temp.replace("\\p{M}".toRegex(), "").lowercase()
    }

}
