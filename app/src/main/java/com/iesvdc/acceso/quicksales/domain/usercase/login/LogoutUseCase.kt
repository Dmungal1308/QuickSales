package com.iesvdc.acceso.quicksales.domain.usercase.login

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke() {
        val prefs = context.getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            remove("jwt_token")
        }
    }
}

