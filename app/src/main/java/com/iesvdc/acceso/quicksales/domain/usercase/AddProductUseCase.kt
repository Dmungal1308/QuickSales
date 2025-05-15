package com.iesvdc.acceso.quicksales.domain.usercase

import com.iesvdc.acceso.quicksales.data.datasource.network.models.ProductResponse
import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val repo: ProductRepository
) {
    suspend operator fun invoke(p: ProductResponse) = repo.addProduct(p)
}