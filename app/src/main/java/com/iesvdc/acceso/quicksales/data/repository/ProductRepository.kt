package com.iesvdc.acceso.quicksales.data.repository

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.iesvdc.acceso.quicksales.data.datasource.network.ProductApi
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio responsable de gestionar las operaciones relacionadas con productos
 * mediante llamadas a la API remota. Proporciona funcionalidades para obtener,
 * añadir, actualizar, eliminar, comprar productos y filtrar productos según distintos criterios.
 *
 * @property productApi Cliente Retrofit para interactuar con el servicio de productos.
 * @property context Contexto de la aplicación, utilizado para acceder a SharedPreferences.
 */
@Singleton
class ProductRepository @Inject constructor(
    private val productApi: ProductApi,
    @ApplicationContext private val context: Context
) {
    /**
     * Mapea la respuesta recibida de la API a un objeto [ProductResponse].
     * Actualmente, solo devuelve la misma instancia. Se deja preparado para
     * transformaciones futuras si fueran necesarias.
     *
     * @param r Respuesta de producto obtenida desde la API.
     * @return Instancia de [ProductResponse] mapeada.
     */
    private fun map(r: ProductResponse): ProductResponse = r

    /**
     * Obtiene los productos creados por el usuario actual (como vendedor) y que aún no han sido comprados.
     *
     * Obtiene la lista completa de productos desde la API, recupera el ID del usuario actual desde
     * SharedPreferences ("user_id"), y filtra aquellos productos cuyo `idVendedor` coincide con el usuario
     * y cuyo `idComprador` es nulo (no vendidos).
     *
     * @return Lista de [ProductResponse] correspondientes a los productos en venta del usuario actual.
     *         En caso de error en la llamada a la API, retorna una lista vacía.
     */
    suspend fun getMyProducts(): List<ProductResponse> {
        return try {
            val all = productApi.getProducts()
            val prefs = context.getSharedPreferences("SessionPrefs", MODE_PRIVATE)
            val meId = prefs.getInt("user_id", -1)
            all.filter { it.idVendedor == meId && it.idComprador == null }
        } catch (e: Exception) {
            Log.e("ProductRepo", "getProducts failed", e)
            emptyList()
        }
    }

    /**
     * Crea un nuevo producto en la plataforma.
     *
     * Recibe un objeto [ProductResponse] con los datos a guardar (nombre, descripción, imagen en Base64, precio),
     * construye una solicitud [ProductRequest] y la envía a la API. Finalmente, mapea la respuesta mediante [map].
     *
     * @param response Objeto [ProductResponse] que contiene los datos del producto a crear.
     * @return [ProductResponse] con la información del producto recién creado, tal como la devuelve la API.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun addProduct(response: ProductResponse): ProductResponse {
        val req = ProductRequest(
            nombre        = response.nombre,
            descripcion   = response.descripcion,
            imagenBase64  = response.imagenBase64,
            precio        = response.precio
        )
        return map(productApi.addProduct(req))
    }

    /**
     * Actualiza un producto existente identificado por su ID.
     *
     * Recibe el ID del producto y un objeto [ProductResponse] con los nuevos datos (nombre, descripción,
     * imagen en Base64, precio), construye una solicitud [ProductRequest] y la envía a la API para su actualización.
     * Finalmente, mapea la respuesta mediante [map].
     *
     * @param id Identificador del producto a actualizar.
     * @param response Objeto [ProductResponse] que contiene los datos nuevos del producto.
     * @return [ProductResponse] con la información actualizada del producto devuelta por la API.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun updateProduct(id: Int, response: ProductResponse): ProductResponse {
        val req = ProductRequest(
            nombre       = response.nombre,
            descripcion  = response.descripcion,
            imagenBase64 = response.imagenBase64,
            precio       = response.precio
        )
        return map(productApi.updateProduct(id, req))
    }

    /**
     * Elimina un producto de la plataforma identificado por su ID.
     *
     * @param id Identificador del producto a eliminar.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun deleteProduct(id: Int) {
        productApi.deleteProduct(id)
    }

    /**
     * Ejecuta la compra de un producto identificado por su ID.
     *
     * Llama a la API para marcar el producto como comprado y asignar el comprador. A continuación,
     * mapea la respuesta mediante [map].
     *
     * @param id Identificador del producto a comprar.
     * @return [ProductResponse] con la información del producto tras la compra.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun purchaseProduct(id: Int): ProductResponse {
        return map(productApi.purchaseProduct(id))
    }

    /**
     * Obtiene la lista de productos disponibles en venta que no pertenecen al usuario actual.
     *
     * Recupera todos los productos desde la API, obtiene el ID del usuario actual desde SharedPreferences,
     * y filtra aquellos cuyo `idVendedor` sea distinto al usuario actual y cuyo estado sea "en venta".
     *
     * @return Lista de [ProductResponse] correspondientes a productos de otros vendedores en estado de venta.
     */
    suspend fun getOtherProducts(): List<ProductResponse> {
        val all = productApi.getProducts()
        val me = context
            .getSharedPreferences("SessionPrefs", MODE_PRIVATE)
            .getInt("user_id", -1)
        return all.filter {
            it.idVendedor != me &&
                    it.estado == "en venta"
        }
    }

    /**
     * Obtiene la lista de productos comprados por el usuario actual.
     *
     * Recupera todos los productos desde la API, obtiene el ID del usuario actual desde SharedPreferences,
     * y filtra aquellos cuyo `idComprador` coincide con el usuario.
     *
     * @return Lista de [ProductResponse] correspondientes a productos adquiridos por el usuario.
     */
    suspend fun getPurchasedProducts(): List<ProductResponse> {
        val all = productApi.getProducts()
        val me = context.getSharedPreferences("SessionPrefs", MODE_PRIVATE)
            .getInt("user_id", -1)
        return all.filter { it.idComprador == me }
    }

    /**
     * Obtiene la lista de productos vendidos por el usuario actual.
     *
     * Recupera todos los productos desde la API, obtiene el ID del usuario actual desde SharedPreferences,
     * y filtra aquellos cuyo `idVendedor` coincide con el usuario y cuyo `idComprador` no es nulo (vendidos).
     *
     * @return Lista de [ProductResponse] correspondientes a productos que el usuario ha vendido.
     */
    suspend fun getSoldProducts(): List<ProductResponse> {
        val all = productApi.getProducts()
        val me = context.getSharedPreferences("SessionPrefs", MODE_PRIVATE)
            .getInt("user_id", -1)
        return all.filter { it.idVendedor == me && it.idComprador != null }
    }

    /**
     * Obtiene la información detallada de un producto por su ID.
     *
     * Llama a la API para recuperar un solo producto identificado por `id`.
     *
     * @param id Identificador del producto a recuperar.
     * @return [ProductResponse] con la información completa del producto.
     * @throws Exception Si ocurre un error en la llamada a la API.
     */
    suspend fun getProductById(id: Int): ProductResponse =
        productApi.getProductById(id)
}
