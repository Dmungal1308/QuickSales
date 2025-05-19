package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.*
import com.iesvdc.acceso.quicksales.domain.models.WalletData
import com.iesvdc.acceso.quicksales.domain.usercase.cartera.DepositUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.GetBalanceUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.login.LogoutUseCase
import com.iesvdc.acceso.quicksales.domain.usercase.cartera.WithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    application: Application,
    private val getBalanceUC: GetBalanceUseCase,
    private val depositUC: DepositUseCase,
    private val withdrawUC: WithdrawUseCase,
    private val logoutUseCase: LogoutUseCase
) : AndroidViewModel(application) {

    private val _balance = MutableLiveData<BigDecimal>()
    val balance: LiveData<BigDecimal> = _balance

    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent



    init {
        loadBalance()
    }

    fun loadBalance() {
        viewModelScope.launch {
            try {
                val wd: WalletData = getBalanceUC()
                _balance.value = wd.saldo
            } catch (e: Exception) {
                _operationResult.value = "Error al obtener saldo"
            }
        }
    }



    fun deposit(amount: BigDecimal) {
        viewModelScope.launch {
            try {
                depositUC(amount)
                _operationResult.value = "Depositado €$amount"
                loadBalance()
            } catch (e: Exception) {
                _operationResult.value = "Error al depositar: ${e.message}"
            }
        }
    }


    fun logout() {
        logoutUseCase()
        _logoutEvent.value = true
    }

    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }

    fun withdraw(amount: BigDecimal) {
        viewModelScope.launch {
            try {
                withdrawUC(amount)
                _operationResult.value = "Ok: retirado €$amount"
                loadBalance()
            } catch (e: Exception) {
                _operationResult.value = "Error al retirar"
            }
        }
    }
}
