package com.example.nalasaka.di

import android.content.Context
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.pref.dataStore
import com.example.nalasaka.data.remote.retrofit.ApiConfig
import com.example.nalasaka.data.repository.UserRepository

object Injection {
    fun provideRepository(context: Context): UserRepository {
        // 1. Get Local Data Source (UserPreference)
        val pref = UserPreference.getInstance(context.dataStore)
        // 2. Get Remote Data Source (ApiService)
        val apiService = ApiConfig.getApiService()
        // 3. Create and return Repository
        return UserRepository.getInstance(apiService, pref)
    }
}