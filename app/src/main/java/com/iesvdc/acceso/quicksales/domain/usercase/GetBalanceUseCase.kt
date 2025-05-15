// File: com/iesvdc/acceso/quicksales/domain/usercase/GetBalanceUseCase.kt
package com.iesvdc.acceso.quicksales.domain.usercase

import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import com.iesvdc.acceso.quicksales.domain.models.WalletData
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import javax.inject.Inject

@ViewModelScoped
class GetBalanceUseCase @Inject constructor(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(): WalletData = withContext(Dispatchers.IO) {
        // BalanceResponse tiene un `saldo: Double`
        val resp = userRepo.getBalance()
        // Lo mapeamos a WalletData con BigDecimal
        WalletData(saldo = resp.saldo)
    }
}
