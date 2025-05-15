package com.iesvdc.acceso.quicksales.domain.usercase

import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject

@ViewModelScoped
class WithdrawUseCase @Inject constructor(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(amount: BigDecimal) = withContext(Dispatchers.IO) {
        val resp = userRepo.withdraw(amount)
        if (!resp.success) throw Exception(resp.message ?: "Retiro fallido")
    }
}