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

@HiltViewModel
class SplashViewModel @Inject constructor(
    application: Application,
    private val waitForSplashUseCase: WaitForSplashUseCase
) : AndroidViewModel(application) {

    private val _navigateToLoginEvent = MutableLiveData<Boolean>()
    val navigateToLoginEvent: LiveData<Boolean> get() = _navigateToLoginEvent

    init {
        viewModelScope.launch {
            _navigateToLoginEvent.value = waitForSplashUseCase()
        }
    }
}
