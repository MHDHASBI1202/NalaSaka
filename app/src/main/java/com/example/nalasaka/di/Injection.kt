package com.example.nalasaka.di

import android.content.Context
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.pref.dataStore
import com.example.nalasaka.data.remote.retrofit.ApiConfig
import com.example.nalasaka.data.repository.UserRepository

object Injection {
    fun provideRepository(context: Context): UserRepository {

        val pref = UserPreference.getInstance(context.dataStore)

        val apiService = ApiConfig.getApiService()

        return UserRepository.getInstance(apiService, pref)
    }
}