package com.iesvdc.acceso.quicksales.domain.usercase.productos.normal

import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class GetMyProductsUseCase @Inject constructor(
    private val repo: ProductRepository
) {
    suspend operator fun invoke(): List<ProductResponse> {
        return repo.getMyProducts()
    }
}
