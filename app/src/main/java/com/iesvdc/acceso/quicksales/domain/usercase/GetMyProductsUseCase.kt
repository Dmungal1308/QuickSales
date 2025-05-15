package com.iesvdc.acceso.quicksales.domain.usercase

import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
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
