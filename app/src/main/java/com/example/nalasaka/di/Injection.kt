package com.example.nalasaka.di

import android.content.Context
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.pref.dataStore // Mengimpor properti ekstensi context.dataStore
import com.example.nalasaka.data.remote.retrofit.ApiConfig
import com.example.nalasaka.data.repository.UserRepository

object Injection {
    fun provideRepository(context: Context): UserRepository {
        // 1. Inisialisasi UserPreference dengan mengirimkan DataStore<Preferences> (context.dataStore)
        val userPreference = UserPreference.getInstance(context.dataStore)

        // 2. Get Remote Data Source (ApiService)
        val apiService = ApiConfig.getApiService()

        // 3. Create and return Repository
        return UserRepository.getInstance(apiService, userPreference)
    }
}