package com.iesvdc.acceso.quicksales.domain.usercase.productos.comprados

import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import javax.inject.Inject

class GetPurchasedProductsUseCase @Inject constructor(
    private val repo: ProductRepository
) {
    suspend operator fun invoke() = repo.getPurchasedProducts()
}
