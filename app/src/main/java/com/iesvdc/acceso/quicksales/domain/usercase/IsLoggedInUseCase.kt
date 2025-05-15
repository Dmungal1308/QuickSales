package com.iesvdc.acceso.quicksales.domain.usercase

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(): Boolean {
        val prefs = context.getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
        val token = prefs.getString("jwt_token", null)
        return !token.isNullOrEmpty()
    }
}

