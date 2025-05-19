package com.iesvdc.acceso.quicksales.data.datasource.network

import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductRequest
import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import retrofit2.http.*

interface ProductApi {

    @GET("productos")
    suspend fun getProducts(): List<ProductResponse>

    @POST("productos")
    suspend fun addProduct(@Body product: ProductRequest): ProductResponse

    @PUT("productos/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body product: ProductRequest
    ): ProductResponse

    @DELETE("productos/{id}")
    suspend fun deleteProduct(@Path("id") id: Int)

    @POST("productos/{id}/comprar")
    suspend fun purchaseProduct(@Path("id") id: Int): ProductResponse
}