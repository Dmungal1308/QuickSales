package com.iesvdc.acceso.quicksales.domain.usercase.login

import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import com.iesvdc.acceso.quicksales.domain.models.WalletData
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class GetBalanceUseCase @Inject constructor(
    private val userRepo: UserRepository
) {
    suspend operator fun invoke(): WalletData = withContext(Dispatchers.IO) {
        val resp = userRepo.getBalance()
        WalletData(saldo = resp.saldo)
    }
}
