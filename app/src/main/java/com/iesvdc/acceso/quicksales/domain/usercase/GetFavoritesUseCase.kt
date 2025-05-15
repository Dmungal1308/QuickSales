package com.iesvdc.acceso.quicksales.domain.usercase

import com.iesvdc.acceso.quicksales.data.repository.FavoriteRepository
import javax.inject.Inject
import dagger.hilt.android.scopes.ViewModelScoped

@ViewModelScoped
class GetFavoritesUseCase @Inject constructor(
    private val repo: FavoriteRepository
) {
    suspend operator fun invoke() = repo.getFavorites()
}