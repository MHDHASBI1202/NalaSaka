package com.example.nalasaka.data.repository

import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.remote.response.AllSakaResponse
import com.example.nalasaka.data.remote.response.DetailSakaResponse
import com.example.nalasaka.data.remote.response.ResponseSaka
import com.example.nalasaka.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    // --- DataStore Operations ---
    fun getUser(): Flow<UserModel> {
        return userPreference.getUser()
    }

    suspend fun saveUser(user: UserModel) {
        userPreference.saveUser(user)
    }

    suspend fun logout() {
        userPreference.logout()
    }

    // --- Remote API Operations ---
    suspend fun register(name: String, email: String, password: String): ResponseSaka {
        return apiService.register(name, email, password)
    }

    suspend fun login(email: String, password: String): ResponseSaka {
        val response = apiService.login(email, password)
        // Auto-save user session on successful login
        if (!response.error && response.loginResult != null) {
            saveUser(
                UserModel(
                    response.loginResult.userId,
                    response.loginResult.name,
                    response.loginResult.token,
                    true
                )
            )
        }
        return response
    }

    suspend fun getAllSaka(token: String): AllSakaResponse {
        // Token perlu di-format menjadi "Bearer TOKEN_VALUE"
        return apiService.getAllSaka("Bearer $token")
    }

    suspend fun getDetailSaka(token: String, sakaId: String): DetailSakaResponse {
        return apiService.getDetailSaka("Bearer $token", sakaId)
    }

    suspend fun addNewSaka(
        token: String,
        file: MultipartBody.Part,
        name: RequestBody,
        description: RequestBody,
        price: RequestBody
    ): ResponseSaka {
        return apiService.addNewSaka("Bearer $token", file, name, description, price)
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference).also { instance = it }
            }
    }
}