package com.example.nalasaka.data.repository

import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.remote.response.*
import com.example.nalasaka.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    // --- DataStore Operations ---
    fun getUser(): Flow<UserModel> = userPreference.getUser()

    suspend fun saveUser(user: UserModel) = userPreference.saveUser(user)

    suspend fun logout() = userPreference.logout()

    // --- Remote API Operations (REAL DATA ONLY) ---

    suspend fun register(name: String, email: String, password: String, phoneNumber: String, address: String, passwordConfirmation: String): ResponseSaka {
        return apiService.register(name, email, phoneNumber, address, password, passwordConfirmation)
    }

    suspend fun login(email: String, password: String): ResponseSaka {
        val response = apiService.login(email, password)
        if (!response.error && response.loginResult != null) {
            saveUser(
                UserModel(
                    userId = response.loginResult.userId,
                    name = response.loginResult.name,
                    token = response.loginResult.token,
                    isLogin = true,
                    role = response.loginResult.role
                )
            )
        }
        return response
    }

    suspend fun getAllSaka(token: String): AllSakaResponse {
        return apiService.getAllSaka("Bearer $token")
    }

    suspend fun getDetailSaka(token: String, sakaId: String): DetailSakaResponse {
        return apiService.getDetailSaka("Bearer $token", sakaId)
    }

    suspend fun addNewSaka(
        token: String,
        file: MultipartBody.Part,
        name: RequestBody,
        category: RequestBody,
        description: RequestBody,
        price: RequestBody,
        stock: RequestBody
    ): ResponseSaka {
        return apiService.addNewSaka("Bearer $token", file, name, category, description, price, stock)
    }

    suspend fun updateStock(token: String, sakaId: String, newStock: Int): ResponseSaka {
        return apiService.updateStock("Bearer $token", sakaId, newStock)
    }

    suspend fun deleteSaka(token: String, sakaId: String): ResponseSaka {
        return apiService.deleteSaka("Bearer $token", sakaId)
    }

    suspend fun getTransactionHistory(token: String, userId: String): TransactionHistoryResponse {
        return apiService.getTransactionHistory("Bearer $token", userId)
    }

    suspend fun checkout(token: String, userId: String, sakaId: String, quantity: Int): CheckoutResponse {
        return apiService.checkoutTransaction("Bearer $token", userId, sakaId, quantity)
    }

    suspend fun getUserProfile(token: String): ProfileResponse {
        return apiService.getUserProfile("Bearer $token")
    }

    suspend fun updateUserProfile(token: String, name: String, phoneNumber: String, address: String, storeName: String? = null): ProfileResponse {
        return apiService.updateUserProfile("Bearer $token", name, phoneNumber, address, storeName)
    }

    suspend fun activateSellerMode(token: String, storeName: String): ProfileResponse {
        return apiService.activateSellerMode("Bearer $token", storeName)
    }

    suspend fun getMyProducts(token: String): AllSakaResponse {
        return apiService.getMyProducts("Bearer $token")
    }

    suspend fun getSellerStats(token: String): SellerStatsResponse {
        return apiService.getSellerStats("Bearer $token")
    }

    // --- MODUL REPUTASI & ANALISIS (NEW) ---

    suspend fun getProductReviews(token: String, sakaId: String): ReviewApiResponse {
        return apiService.getProductReviews("Bearer $token", sakaId)
    }

    suspend fun addReview(
        token: String,
        sakaId: RequestBody,
        rating: RequestBody,
        comment: RequestBody,
        file: MultipartBody.Part?
    ): ResponseSaka {
        return apiService.addReview("Bearer $token", sakaId, rating, comment, file)
    }

    suspend fun uploadCertification(token: String, file: MultipartBody.Part): ProfileResponse {
        return apiService.uploadCertification("Bearer $token", file)
    }

    suspend fun toggleWishlist(token: String, sakaId: String): WishlistResponse {
        return apiService.toggleWishlist("Bearer $token", sakaId)
    }

    suspend fun checkWishlist(token: String, sakaId: String): WishlistResponse {
        return apiService.checkWishlist("Bearer $token", sakaId)
    }

    suspend fun getMyWishlist(token: String): AllSakaResponse {
        return apiService.getMyWishlist("Bearer $token")
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(apiService: ApiService, userPreference: UserPreference): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference).also { instance = it }
            }
    }
}