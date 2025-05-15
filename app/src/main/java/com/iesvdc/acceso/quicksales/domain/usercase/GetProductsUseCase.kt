package com.iesvdc.acceso.quicksales.domain.usercase

import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repo: ProductRepository
) {
    suspend operator fun invoke() = repo.getMyProducts()
}