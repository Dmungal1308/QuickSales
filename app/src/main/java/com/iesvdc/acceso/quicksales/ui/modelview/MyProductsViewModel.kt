package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.AddProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.DeleteProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.GetMyProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.UpdateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.Normalizer
import javax.inject.Inject

/**
 * ViewModel encargado de manejar la lógica de negocio para los productos propios del usuario.
 *
 * Utiliza casos de uso para:
 *  - Obtener los productos del usuario ([GetMyProductsUseCase]).
 *  - Crear un nuevo producto ([AddProductUseCase]).
 *  - Eliminar un producto existente ([DeleteProductUseCase]).
 *  - Actualizar un producto existente ([UpdateProductUseCase]).
 *
 * Expone LiveData para:
 *  - Lista de productos actuales (_products).
 *  - Resultados de las operaciones de creación, actualización y eliminación (_operationResult).
 *  - Evento de logout (_logoutEvent).
 *
 * @param application Contexto de la aplicación, necesario para heredar de AndroidViewModel.
 * @param getMyProductsUseCase Caso de uso para obtener la lista de productos propios.
 * @param createProductUseCase Caso de uso para crear un nuevo producto.
 * @param deleteProductUseCase Caso de uso para eliminar un producto.
 * @param updateProductUseCase Caso de uso para actualizar un producto.
 */
@HiltViewModel
class MyProductsViewModel @Inject constructor(
    application: Application,
    private val getMyProductsUseCase: GetMyProductsUseCase,
    private val createProductUseCase: AddProductUseCase,
    private val deleteProductUseCase: DeleteProductUseCase,
    private val updateProductUseCase: UpdateProductUseCase
) : AndroidViewModel(application) {

    /** Lista interna que almacena todos los productos propios para permitir filtrados. */
    private val allProducts = mutableListOf<ProductResponse>()

    /**
     * LiveData que expone la lista de productos filtrados o completos según corresponda.
     */
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    /**
     * LiveData que expone mensajes sobre el resultado de las operaciones de creación,
     * actualización y eliminación de productos.
     */
    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> get() = _operationResult

    /**
     * LiveData que notifica un evento de logout (manejado fuera de este ViewModel).
     */
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    init {
        // Al inicializar el ViewModel, cargamos los productos del usuario.
        loadMyProducts()
    }

    /**
     * Carga los productos propios del usuario desde la fuente de datos.
     *
     * - Llama a [getMyProductsUseCase] para obtener la lista de productos.
     * - Filtra aquellos cuyo `idComprador` es null (productos aún en venta).
     * - Actualiza [allProducts] y publica la lista filtrada en [_products].
     */
    fun loadMyProducts() {
        viewModelScope.launch {
            val list = getMyProductsUseCase()
                .filter { it.idComprador == null }

            allProducts.clear()
            allProducts += list
            _products.value = list
        }
    }

    /**
     * Filtra la lista de productos por nombre usando la cadena de búsqueda proporcionada.
     *
     * - Normaliza la cadena eliminando diacríticos y convirtiendo a minúsculas.
     * - Si la consulta está vacía, muestra todos los productos almacenados en [allProducts].
     * - En caso contrario, filtra [allProducts] manteniendo aquellos cuyo nombre contenga la subcadena normalizada.
     *
     * @param query Texto ingresado por el usuario para filtrar productos.
     */
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

    /**
     * Crea un nuevo producto utilizando los datos proporcionados en [p].
     *
     * - Llama a [createProductUseCase] con el objeto [ProductResponse] recibido.
     * - Si la operación es exitosa, publica "Producto creado" en [_operationResult]
     *   y recarga los productos llamando a [loadMyProducts].
     * - Si ocurre un error, publica un mensaje descriptivo en [_operationResult].
     *
     * @param p Objeto [ProductResponse] con los datos del producto a crear.
     */
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

    /**
     * Actualiza el producto identificado por [id] con los datos proporcionados en [p].
     *
     * - Llama a [updateProductUseCase] con el ID y el objeto [ProductResponse].
     * - Si la operación es exitosa, publica "Producto actualizado" en [_operationResult]
     *   y recarga los productos llamando a [loadMyProducts].
     * - Si ocurre un [HttpException], publica "Error servidor (<código>)" en [_operationResult].
     * - Para otros errores, publica un mensaje descriptivo en [_operationResult].
     *
     * @param id ID del producto a actualizar.
     * @param p  Objeto [ProductResponse] con los nuevos datos del producto.
     */
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

    /**
     * Elimina el producto identificado por [id].
     *
     * - Llama a [deleteProductUseCase] con el ID del producto.
     * - Si la operación es exitosa, publica "Producto eliminado" en [_operationResult]
     *   y recarga los productos llamando a [loadMyProducts].
     * - Si ocurre un [HttpException], publica "Error servidor (<código>)" en [_operationResult].
     * - Para otros errores, publica un mensaje descriptivo en [_operationResult].
     *
     * @param id ID del producto a eliminar.
     */
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

    /**
     * Reinicia el evento de logout estableciendo [_logoutEvent] a false.
     */
    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

    /**
     * Normaliza [text] eliminando diacríticos y convirtiendo a minúsculas.
     *
     * Útil para comparaciones insensibles a acentos y mayúsculas durante el filtrado.
     *
     * @param text Cadena de texto a normalizar.
     * @return Cadena normalizada sin diacríticos y en minúsculas.
     */
    private fun normalize(text: String): String {
        val temp = Normalizer.normalize(text, Normalizer.Form.NFD)
        return temp.replace("\\p{M}".toRegex(), "").lowercase()
    }
}
