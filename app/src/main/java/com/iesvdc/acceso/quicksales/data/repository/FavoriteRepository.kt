package com.iesvdc.acceso.quicksales.data.repository

import com.iesvdc.acceso.quicksales.data.datasource.network.FavoriteApi
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.FavoriteRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(
    private val api: FavoriteApi
) {
    suspend fun getFavorites() = api.getFavorites()
    suspend fun addFavorite(idProducto: Int) = api.addFavorite(FavoriteRequest(idProducto))
    suspend fun removeFavorite(idProducto: Int) = api.removeFavorite(idProducto)
}
