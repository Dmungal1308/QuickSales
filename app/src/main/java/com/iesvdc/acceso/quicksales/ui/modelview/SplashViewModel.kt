package com.iesvdc.acceso.quicksales.ui.modelview

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iesvdc.acceso.quicksales.domain.usercase.WaitForSplashUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel encargado de la pantalla de Splash.
 *
 * Utiliza [WaitForSplashUseCase] para determinar si, tras la pantalla de Splash,
 * se debe navegar a la pantalla de inicio de sesión. Publica el resultado a través
 * de [navigateToLoginEvent].
 *
 * @param application Contexto de la aplicación, necesario para heredar de AndroidViewModel.
 * @param waitForSplashUseCase Caso de uso que espera un tiempo o realiza inicializaciones
 *                             antes de decidir la navegación desde Splash.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    application: Application,
    private val waitForSplashUseCase: WaitForSplashUseCase
) : AndroidViewModel(application) {

    /**
     * LiveData que notifica si se debe navegar a la pantalla de login.
     * - `true` cuando finaliza la espera o inicialización y se debe ir a Login.
     * - `false` en caso contrario (aunque habitualmente solo se emite `true`).
     */
    private val _navigateToLoginEvent = MutableLiveData<Boolean>()
    val navigateToLoginEvent: LiveData<Boolean> get() = _navigateToLoginEvent

    init {
        // Al crear el ViewModel, se lanza la corrutina que ejecuta el caso de uso
        // y publica el resultado en _navigateToLoginEvent.
        viewModelScope.launch {
            _navigateToLoginEvent.value = waitForSplashUseCase()
        }
    }
}
