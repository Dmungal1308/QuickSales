package com.iesvdc.acceso.quicksales.domain.usercase.productos.favoritos

import com.iesvdc.acceso.quicksales.data.repository.FavoriteRepository
import javax.inject.Inject
import dagger.hilt.android.scopes.ViewModelScoped

@ViewModelScoped
class AddFavoriteUseCase @Inject constructor(
    private val repo: FavoriteRepository
) {
    suspend operator fun invoke(idProducto: Int) = repo.addFavorite(idProducto)
}