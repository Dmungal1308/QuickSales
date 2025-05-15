package com.iesvdc.acceso.quicksales.domain.usercase

import kotlinx.coroutines.delay
import javax.inject.Inject

class WaitForSplashUseCase @Inject constructor() {
    suspend operator fun invoke(delayTimeMillis: Long = 1000): Boolean {
        delay(delayTimeMillis)
        return true
    }
}
