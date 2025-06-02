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

/**
 * ViewModel encargado de gestionar la lógica principal del menú de productos,
 * incluyendo la carga de productos disponibles, la gestión de favoritos y compras.
 *
 * Utiliza los casos de uso:
 * - [GetOtherProductsUseCase] para obtener los productos de otros usuarios.
 * - [GetFavoritesUseCase] para obtener los IDs de productos marcados como favoritos.
 * - [AddFavoriteUseCase] y [RemoveFavoriteUseCase] para alternar el estado de favorito.
 * - [PurchaseProductUseCase] para ejecutar la compra de un producto.
 *
 * Expone LiveData para:
 * - Lista de productos disponibles (_products).
 * - Conjunto de IDs de favoritos (_favoriteIdsLive).
 * - Eventos de logout (_logoutEvent).
 * - Mensajes de error y éxito en la compra (_purchaseError y _purchaseSuccess).
 *
 * @param application Contexto de la aplicación, necesario para heredar de AndroidViewModel.
 * @param getOtherProductsUseCase Caso de uso para recuperar productos de otros usuarios.
 * @param purchaseProductUseCase Caso de uso para ejecutar la compra de un producto.
 * @param getFavoritesUseCase Caso de uso para obtener IDs de productos favoritos.
 * @param addFavoriteUseCase Caso de uso para agregar un producto a favoritos.
 * @param removeFavoriteUseCase Caso de uso para eliminar un producto de favoritos.
 */
@HiltViewModel
class MenuViewModel @Inject constructor(
    application: Application,
    private val getOtherProductsUseCase: GetOtherProductsUseCase,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val addFavoriteUseCase: AddFavoriteUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase
) : AndroidViewModel(application) {

    /** Lista interna mutable que contiene todos los productos obtenidos. */
    private val allProducts = mutableListOf<ProductResponse>()

    /**
     * LiveData que expone la lista de productos disponibles para mostrar en el menú.
     */
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    /** Conjunto mutable que almacena los IDs de productos marcados como favoritos. */
    private val favoriteIds = mutableSetOf<Int>()

    /**
     * LiveData que expone el conjunto de IDs de productos actualmente marcados como favoritos.
     */
    private val _favoriteIdsLive = MutableLiveData<Set<Int>>()
    val favoriteIdsLive: LiveData<Set<Int>> = _favoriteIdsLive

    /**
     * LiveData que notifica un evento de logout. Puede usarse para retornar a pantalla de login.
     */
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    /**
     * LiveData que contiene mensajes de error al intentar comprar un producto.
     * Si es null, no hay error; de lo contrario, muestra la descripción del problema.
     */
    private val _purchaseError   = MutableLiveData<String?>()
    val purchaseError: LiveData<String?> = _purchaseError

    /**
     * LiveData que indica si la compra se realizó con éxito.
     * Se establece a true tras una compra exitosa y debe limpiarse posteriormente.
     */
    private val _purchaseSuccess = MutableLiveData<Boolean>()
    val purchaseSuccess: LiveData<Boolean> = _purchaseSuccess

    init {
        // Al inicializar el ViewModel, cargamos los datos de productos y favoritos.
        loadData()
    }

    /**
     * Intenta comprar el producto identificado por [productId].
     *
     * - Si la compra es exitosa:
     *   1. Publica `true` en [_purchaseSuccess].
     *   2. Recarga los datos llamando a [loadData] para reflejar el cambio.
     *
     * - Si ocurre una [HttpException]:
     *   - Si el código es 400, establece el mensaje "No tienes saldo suficiente" en [_purchaseError].
     *   - En otro caso, muestra el mensaje de excepción.
     *
     * - Si ocurre cualquier otra excepción, muestra el mensaje genérico en [_purchaseError].
     *
     * @param productId ID del producto que se desea comprar.
     */
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

    /** Reinicia el mensaje de error de compra estableciendo [_purchaseError] a null. */
    fun clearPurchaseError()   { _purchaseError.value = null }

    /** Reinicia el indicador de compra exitosa estableciendo [_purchaseSuccess] a false. */
    fun clearPurchaseSuccess() { _purchaseSuccess.value = false }

    /**
     * Carga los productos y favoritos para el menú:
     * 1. Obtiene todos los productos de otros usuarios con [getOtherProductsUseCase].
     * 2. Obtiene los IDs de productos favoritos con [getFavoritesUseCase].
     * 3. Actualiza [favoriteIds] y publica el nuevo conjunto en [_favoriteIdsLive].
     * 4. Llena [allProducts] con la lista completa de productos y la publica en [_products].
     */
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

    /**
     * Filtra la lista de productos por nombre usando [query] como término de búsqueda.
     *
     * - Normaliza la cadena eliminando diacríticos y convirtiendo a minúsculas.
     * - Si la consulta está vacía, muestra todos los productos.
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
     * Alterna el estado de favorito del [product] proporcionado.
     *
     * - Si el ID ya está en [favoriteIds], llama a [removeFavoriteUseCase] y lo elimina de [favoriteIds].
     * - Si no está, llama a [addFavoriteUseCase] y lo agrega a [favoriteIds].
     * - Finalmente, publica el nuevo conjunto en [_favoriteIdsLive].
     *
     * @param product Instancia de [ProductResponse] cuyo estado de favorito se alternará.
     */
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

    /** Reinicia el evento de logout estableciendo [_logoutEvent] a false. */
    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

    /**
     * Normaliza [text] eliminando diacríticos y convirtiendo a minúsculas.
     *
     * Útil para comparaciones insensibles a acentos y mayúsculas.
     *
     * @param text Cadena de texto a normalizar.
     * @return Cadena normalizada sin diacríticos y en minúsculas.
     */
    private fun normalize(text: String): String {
        val temp = Normalizer.normalize(text, Normalizer.Form.NFD)
        return temp.replace("\\p{M}".toRegex(), "").lowercase()
    }
}
