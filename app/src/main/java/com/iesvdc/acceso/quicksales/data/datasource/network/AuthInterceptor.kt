package com.iesvdc.acceso.quicksales.data.datasource.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    @ApplicationContext context: Context
) : Interceptor {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = prefs.getString("jwt_token", null)
        Log.d("AuthInterceptor", "Token enviado en header: $token")
        val requestBuilder = chain.request().newBuilder()
        token?.let {
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }


}
