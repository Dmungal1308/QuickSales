package com.iesvdc.acceso.quicksales.ui.modelview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iesvdc.acceso.quicksales.data.datasource.network.models.usuarios.UserResponse
import com.iesvdc.acceso.quicksales.domain.usercase.usuarios.GetUserByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SellerViewModel @Inject constructor(
    private val getUserByIdUseCase: GetUserByIdUseCase
) : ViewModel() {
    private val _user = MutableLiveData<UserResponse>()
    val user: LiveData<UserResponse> = _user

    fun loadUser(id: Int) = viewModelScope.launch {
        _user.value = getUserByIdUseCase(id)!!
    }
}
