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

/**
 * ViewModel encargado de gestionar la información del saldo del usuario (cartera),
 * así como las operaciones de depósito y retiro de fondos.
 *
 * Utiliza los casos de uso:
 *  - [GetBalanceUseCase] para obtener el saldo actual del usuario.
 *  - [DepositUseCase] para realizar depósitos.
 *  - [WithdrawUseCase] para realizar retiros.
 *
 * Expone LiveData para:
 *  - [balance]: saldo actual del usuario.
 *  - [operationResult]: mensajes resultantes de las operaciones (depósito/retirada/errores).
 *  - [logoutEvent]: evento para notificar un logout (si se implementa esa lógica).
 *
 * @param application Contexto de la aplicación, necesario para heredar de AndroidViewModel.
 * @param getBalanceUC Caso de uso para recuperar el saldo actual de la cartera.
 * @param depositUC Caso de uso para ejecutar un depósito de fondos.
 * @param withdrawUC Caso de uso para ejecutar un retiro de fondos.
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    application: Application,
    private val getBalanceUC: GetBalanceUseCase,
    private val depositUC: DepositUseCase,
    private val withdrawUC: WithdrawUseCase,
) : AndroidViewModel(application) {

    /**
     * LiveData que contiene el saldo actual de la cartera del usuario.
     * - Se actualiza al llamar a [loadBalance], [deposit] o [withdraw].
     */
    private val _balance = MutableLiveData<BigDecimal>()
    val balance: LiveData<BigDecimal> = _balance

    /**
     * LiveData que contiene mensajes sobre el resultado de las operaciones:
     * - Mensaje de éxito o error tras un depósito o retiro.
     * - Mensaje de error si falla la carga del saldo.
     */
    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    /**
     * LiveData que notifica un evento de logout.
     * - Actualmente no utilizado directamente en este ViewModel,
     *   pero preparado para notificar un cierre de sesión si se requiere.
     */
    private val _logoutEvent = MutableLiveData<Boolean>()
    val logoutEvent: LiveData<Boolean> = _logoutEvent

    init {
        // Al inicializar el ViewModel, cargamos el saldo actual del usuario.
        loadBalance()
    }

    /**
     * Carga el saldo actual de la cartera del usuario.
     *
     * - Ejecuta [getBalanceUC] en una corrutina de [viewModelScope].
     * - Si la llamada es exitosa, publica el saldo en [_balance].
     * - Si ocurre una excepción, publica un mensaje de error en [_operationResult].
     */
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

    /**
     * Realiza un depósito de la cantidad especificada en la cartera del usuario.
     *
     * - Recibe [amount] como la cantidad a depositar (BigDecimal).
     * - Ejecuta [depositUC] en una corrutina de [viewModelScope].
     * - Si la operación es exitosa:
     *     1. Publica en [_operationResult] el mensaje "Depositado €[amount]".
     *     2. Llama a [loadBalance] para actualizar el saldo.
     * - Si ocurre una excepción, publica en [_operationResult] un mensaje de error con la descripción.
     *
     * @param amount Cantidad a depositar en formato BigDecimal.
     */
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

    /**
     * Realiza un retiro de la cantidad especificada de la cartera del usuario.
     *
     * - Recibe [amount] como la cantidad a retirar (BigDecimal).
     * - Ejecuta [withdrawUC] en una corrutina de [viewModelScope].
     * - Si la operación es exitosa:
     *     1. Publica en [_operationResult] el mensaje "Ok: retirado €[amount]".
     *     2. Llama a [loadBalance] para actualizar el saldo.
     * - Si ocurre una excepción, publica en [_operationResult] el mensaje "Error al retirar".
     *
     * @param amount Cantidad a retirar en formato BigDecimal.
     */
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

    /**
     * Reinicia el evento de logout estableciendo [_logoutEvent] a false.
     *
     * - Útil para limpiar o resetear el estado del evento luego de que la UI lo consuma.
     */
    fun resetLogoutEvent() {
        _logoutEvent.value = false
    }
}
