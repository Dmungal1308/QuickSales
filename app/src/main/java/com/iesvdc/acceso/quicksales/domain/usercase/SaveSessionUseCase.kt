package com.iesvdc.acceso.quicksales.domain.usercase

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SaveSessionUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(userId: Int) {
        val prefs = context.getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean("isLoggedIn", true)
            putInt("user_id", userId)
        }
    }
}

