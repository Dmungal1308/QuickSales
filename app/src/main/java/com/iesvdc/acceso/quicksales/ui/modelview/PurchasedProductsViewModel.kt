package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.productos.comprados.GetPurchasedProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchasedProductsViewModel @Inject constructor(
    application: Application,
    private val getPurchasedProductsUseCase: GetPurchasedProductsUseCase
) : AndroidViewModel(application) {

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products
    init { loadPurchased() }
    fun loadPurchased() = viewModelScope.launch {
        _products.value = getPurchasedProductsUseCase()!!
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }
}
