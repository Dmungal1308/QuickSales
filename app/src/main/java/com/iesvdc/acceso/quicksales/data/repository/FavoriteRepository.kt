package com.iesvdc.acceso.quicksales.data.repository

import com.iesvdc.acceso.quicksales.data.datasource.network.FavoriteApi
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.FavoriteRequest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio responsable de gestionar las operaciones relacionadas con
 * favoritos de productos a través de llamadas a la API remota.
 *
 * @property api Cliente Retrofit para interactuar con el servicio de favoritos.
 */
@Singleton
class FavoriteRepository @Inject constructor(
    private val api: FavoriteApi
) {
    /**
     * Obtiene la lista de productos marcados como favoritos por el usuario.
     *
     * @return Una lista de objetos que representan los productos favoritos.
     * @throws Exception Si ocurre un error en la llamada a la API o la respuesta no es válida.
     */
    suspend fun getFavorites() = api.getFavorites()

    /**
     * Agrega un producto a los favoritos del usuario.
     *
     * @param idProducto Identificador del producto que se desea agregar a favoritos.
     * @return El resultado de la operación de agregar favorito.
     * @throws Exception Si ocurre un error en la llamada a la API o la respuesta no es válida.
     */
    suspend fun addFavorite(idProducto: Int) = api.addFavorite(FavoriteRequest(idProducto))

    /**
     * Elimina un producto de los favoritos del usuario.
     *
     * @param idProducto Identificador del producto que se desea eliminar de favoritos.
     * @return El resultado de la operación de eliminación de favorito.
     * @throws Exception Si ocurre un error en la llamada a la API o la respuesta no es válida.
     */
    suspend fun removeFavorite(idProducto: Int) = api.removeFavorite(idProducto)
}
