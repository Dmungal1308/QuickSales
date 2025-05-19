package com.iesvdc.acceso.quicksales.domain.usercase.productos.normal

import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import javax.inject.Inject

class PurchaseProductUseCase @Inject constructor(
    private val repo: ProductRepository
) {
    suspend operator fun invoke(id: Int) = repo.purchaseProduct(id)
}