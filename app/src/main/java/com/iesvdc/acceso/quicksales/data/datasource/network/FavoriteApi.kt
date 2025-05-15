package com.iesvdc.acceso.quicksales.data.datasource.network

import com.iesvdc.acceso.quicksales.data.datasource.network.models.FavoriteRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.FavoriteResponse
import retrofit2.http.*



interface FavoriteApi {
    @GET("favoritos")
    suspend fun getFavorites(): List<FavoriteResponse>

    @POST("favoritos")
    suspend fun addFavorite(@Body req: FavoriteRequest): FavoriteResponse

    @DELETE("favoritos/{idProducto}")
    suspend fun removeFavorite(@Path("idProducto") idProducto: Int): Map<String, Boolean>
}
