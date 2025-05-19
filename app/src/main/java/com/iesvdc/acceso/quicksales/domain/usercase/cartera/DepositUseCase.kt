package com.iesvdc.acceso.quicksales.domain.usercase.cartera

import com.iesvdc.acceso.quicksales.data.datasource.network.models.login.OperationResponse
import com.iesvdc.acceso.quicksales.data.repository.UserRepository
import dagger.hilt.android.scopes.ViewModelScoped
import java.math.BigDecimal
import javax.inject.Inject

@ViewModelScoped
class DepositUseCase @Inject constructor(
    private val repo: UserRepository
) {
    suspend operator fun invoke(amount: BigDecimal): OperationResponse {
        return repo.deposit(amount)
    }
}
