package com.iesvdc.acceso.quicksales.domain.usercase.productos.vendidos

import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import javax.inject.Inject

class GetSoldProductsUseCase @Inject constructor(
    private val repo: ProductRepository
) {
    suspend operator fun invoke() = repo.getSoldProducts()
}