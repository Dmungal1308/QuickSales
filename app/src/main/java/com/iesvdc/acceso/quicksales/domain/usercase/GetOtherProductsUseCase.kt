package com.iesvdc.acceso.quicksales.domain.usercase

import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import javax.inject.Inject

class GetOtherProductsUseCase @Inject constructor(
    private val repo: ProductRepository
) {
    suspend operator fun invoke(): List<ProductResponse> =
        repo.getOtherProducts()
}