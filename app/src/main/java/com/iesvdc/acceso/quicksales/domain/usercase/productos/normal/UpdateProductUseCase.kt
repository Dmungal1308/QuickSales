package com.iesvdc.acceso.quicksales.domain.usercase.productos.normal

import com.iesvdc.acceso.quicksales.data.datasource.network.models.productos.ProductResponse
import com.iesvdc.acceso.quicksales.data.repository.ProductRepository
import javax.inject.Inject

class UpdateProductUseCase @Inject constructor(
    private val repo: ProductRepository
) {
    suspend operator fun invoke(id: Int, p: ProductResponse) = repo.updateProduct(id, p)
}