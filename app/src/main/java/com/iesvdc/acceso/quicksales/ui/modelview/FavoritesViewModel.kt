package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.GetFavoritesUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.GetOtherProductsUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.normal.PurchaseProductUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos.RemoveFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.text.Normalizer
import javax.inject.Inject

/**
 * ViewModel encargado de gestionar los productos marcados como favoritos,
 * así como las operaciones de compra y filtrado dentro de la sección de favoritos.
 *
 * Usa casos de uso para:
 *  - Obtener todos los productos disponibles (GetOtherProductsUseCase)
 *  - Obtener IDs de favoritos del usuario (GetFavoritesUseCase)
 *  - Comprar un producto (PurchaseProductUseCase)
 *  - Eliminar un producto de favoritos (RemoveFavoriteUseCase)
 *
 * @property getOtherProductsUseCase Caso de uso para obtener la lista de productos ajenos al usuario.
 * @property getFavoritesUseCase Caso de uso para obtener la lista de IDs de productos favoritos.
 * @property purchaseProductUseCase Caso de uso para ejecutar la compra de un producto.
 * @property removeFavoriteUseCase Caso de uso para eliminar un producto de la lista de favoritos.
 */
@HiltViewModel
class FavoritesViewModel @Inject constructor(
    application: Application,
    private val getOtherProductsUseCase: GetOtherProductsUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val purchaseProductUseCase: PurchaseProductUseCase,
    private val removeFavoriteUseCase: RemoveFavoriteUseCase
) : AndroidViewModel(application) {

    /**
     * LiveData que contiene la lista de productos favoritos filtrados y disponibles para mostrar.
     */
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    /**
     * LiveData que notifica un evento de logout (manejado fuera de este ViewModel).
     */
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    /**
     * LiveData que mantiene el conjunto de IDs de productos actualmente marcados como favoritos.
     */
    private val _favoriteIds = MutableLiveData<Set<Int>>(emptySet())
    val favoriteIds: LiveData<Set<Int>> = _favoriteIds

    /** Lista interna que almacena todos los productos favoritos cargados para filtrado. */
    private var allFavorites: List<ProductResponse> = emptyList()

    /**
     * LiveData que contiene mensajes de error al intentar comprar un producto.
     */
    private val _purchaseError = MutableLiveData<String?>()
    val purchaseError: LiveData<String?> = _purchaseError

    init {
        // Al inicializar el ViewModel, cargamos los productos favoritos.
        loadFavorites()
    }

    /**
     * Intenta comprar el producto con el ID especificado.
     *
     * - Si la compra es exitosa, recarga la lista de favoritos.
     * - Si ocurre un error HttpException, intenta parsear el cuerpo para mensajes específicos
     *   (p.ej., "Saldo insuficiente") y publica un mensaje apropiado en [purchaseError].
     * - Para otros errores, muestra un mensaje genérico.
     *
     * @param id ID del producto que se desea comprar.
     */
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

    /**
     * Limpia el mensaje de error de compra, estableciendo [purchaseError] a null.
     */
    fun clearPurchaseError() {
        _purchaseError.value = null
    }

    /**
     * Carga la lista de productos favoritos:
     * 1. Obtiene todos los productos disponibles a través de [getOtherProductsUseCase].
     * 2. Obtiene el conjunto de IDs de favoritos con [getFavoritesUseCase].
     * 3. Filtra los productos cuyo ID esté en el conjunto de favoritos y que no hayan sido comprados.
     * 4. Publica la lista filtrada en [_products] y actualiza [allFavorites] y [_favoriteIds].
     */
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

    /**
     * Filtra la lista de favoritos cargados según el texto de búsqueda proporcionado.
     *
     * Normaliza la cadena de búsqueda eliminando diacríticos y comparando en minúsculas.
     * Si la consulta está vacía, muestra todos los favoritos; de lo contrario, filtra por nombre.
     *
     * @param query Cadena de búsqueda para filtrar productos por nombre.
     */
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

    /**
     * Elimina el producto pasado de la lista de favoritos.
     *
     * - Llama a [removeFavoriteUseCase] con el ID del producto.
     * - Luego recarga la lista de favoritos para reflejar el cambio.
     *
     * @param product Producto de tipo [ProductResponse] a eliminar de favoritos.
     */
    fun removeFavorite(product: ProductResponse) {
        viewModelScope.launch {
            removeFavoriteUseCase(product.id)
            loadFavorites()
        }
    }

    /**
     * Reinicia el evento de logout estableciendo [_logoutEvent] a false.
     */
    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }
}
