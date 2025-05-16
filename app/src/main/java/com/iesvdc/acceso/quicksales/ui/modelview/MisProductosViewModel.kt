// File: com/iesvdc/acceso/quicksales/ui/modelview/MisProductosViewModel.kt
package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.Normalizer
import javax.inject.Inject

@HiltViewModel
class MisProductosViewModel @Inject constructor(
    application: Application,
    private val getMyProductsUseCase: GetMyProductsUseCase,
    private val createProductUseCase: AddProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val logoutUseCase: LogoutUseCase
) : AndroidViewModel(application) {

    private val allProducts = mutableListOf<ProductResponse>()
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    init { loadMyProducts() }

    fun loadMyProducts() {
        viewModelScope.launch {
            val list = getMyProductsUseCase()
            allProducts.clear()
            allProducts += list
            _products.value = list
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

    fun createProduct(p: ProductResponse) {
        viewModelScope.launch {
            try {
                createProductUseCase(p)
                _operationResult.value = "Producto creado"
                loadMyProducts()
            } catch (e: Exception) {
                _operationResult.value = "Error al crear: ${e.message}"
            }
        }
    }

    fun updateProduct(id: Int, p: ProductResponse) {
        viewModelScope.launch {
            try {
                updateProductUseCase(id, p)
                _operationResult.value = "Producto actualizado"
                loadMyProducts()
            } catch (e: HttpException) {
                _operationResult.value = "Error servidor (${e.code()})"
            } catch (e: Exception) {
                _operationResult.value = "Error al actualizar: ${e.message}"
            }
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            try {
                deleteProductUseCase(id)
                _operationResult.value = "Producto eliminado"
                loadMyProducts()
            } catch (e: HttpException) {
                _operationResult.value = "Error servidor (${e.code()})"
            } catch (e: Exception) {
                _operationResult.value = "Error al eliminar: ${e.message}"
            }
        }
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
