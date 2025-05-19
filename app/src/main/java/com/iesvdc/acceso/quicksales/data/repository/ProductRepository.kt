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

@Singleton
class ProductRepository @Inject constructor(
    private val productApi: ProductApi,
    @ApplicationContext private val context: Context
) {
    private fun map(r: ProductResponse): ProductResponse = r

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



    suspend fun addProduct(response: ProductResponse): ProductResponse {
        val req = ProductRequest(
            nombre        = response.nombre,
            descripcion   = response.descripcion,
            imagenBase64  = response.imagenBase64,
            precio        = response.precio
        )
        return map(productApi.addProduct(req))
    }

    suspend fun updateProduct(id: Int, response: ProductResponse): ProductResponse {
        val req = ProductRequest(
            nombre       = response.nombre,
            descripcion  = response.descripcion,
            imagenBase64 = response.imagenBase64,
            precio       = response.precio
        )
        return map(productApi.updateProduct(id, req))
    }

    suspend fun deleteProduct(id: Int) {
        productApi.deleteProduct(id)
    }

    suspend fun purchaseProduct(id: Int): ProductResponse {
        return map(productApi.purchaseProduct(id))
    }

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

    suspend fun getPurchasedProducts(): List<ProductResponse> {
        val all = productApi.getProducts()
        val me = context.getSharedPreferences("SessionPrefs", MODE_PRIVATE)
            .getInt("user_id", -1)
        return all.filter { it.idComprador == me }
    }


}