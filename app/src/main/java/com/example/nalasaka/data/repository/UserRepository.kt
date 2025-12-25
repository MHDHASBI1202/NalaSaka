package com.example.nalasaka.data.repository

import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.remote.response.*
import com.example.nalasaka.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    fun getUser(): Flow<UserModel> = userPreference.getUser()

    suspend fun saveUser(user: UserModel) = userPreference.saveUser(user)

    suspend fun logout() = userPreference.logout()


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
        discountPrice: RequestBody?,
        stock: RequestBody
    ): ResponseSaka {
        return apiService.addNewSaka("Bearer $token", file, name, category, description, price, discountPrice, stock)
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

    suspend fun updateProfilePhoto(token: String, file: MultipartBody.Part): ResponseSaka {
        return apiService.updateProfilePhoto("Bearer $token", file)
    }

    suspend fun checkout(token: String, userId: String, sakaId: String, quantity: Int, paymentMethod: String): CheckoutResponse {
        return apiService.checkoutTransaction("Bearer $token", userId, sakaId, quantity, paymentMethod)
    }

    suspend fun getUserProfile(token: String): ProfileResponse {
        return apiService.getUserProfile("Bearer $token")
    }

    suspend fun updateUserProfile(token: String, name: String, phoneNumber: String, address: String, storeName: String? = null): ProfileResponse {
        return apiService.updateUserProfile("Bearer $token", name, phoneNumber, address, storeName)
    }

    suspend fun updateStatus(token: String, transactionId: Int, status: String): ResponseSaka {
        return apiService.updateOrderStatus("Bearer $token", transactionId, status)
    }

    suspend fun getAuthToken(): String {
        return userPreference.getUser().first().token
    }

    suspend fun activateSellerMode(token: String, storeName: String): ProfileResponse {
        return apiService.activateSellerMode("Bearer $token", storeName)
    }

    suspend fun getMyProducts(token: String): AllSakaResponse {
        return apiService.getMyProducts(token)
    }

    suspend fun getSellerStats(token: String): SellerStatsResponse {
        return apiService.getSellerStats(token)
    }


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

    suspend fun getCart(token: String) = apiService.getCart("Bearer $token")
    suspend fun addToCart(token: String, sakaId: String, qty: Int) = apiService.addToCart("Bearer $token", sakaId, qty)
    suspend fun updateCartQty(token: String, cartId: Int, qty: Int) = apiService.updateCartQty("Bearer $token", cartId, qty)
    suspend fun deleteCartItem(token: String, cartId: Int) = apiService.deleteCartItem("Bearer $token", cartId)
    suspend fun checkoutCart(token: String, paymentMethod: String) =
        apiService.checkoutCart("Bearer $token", paymentMethod)

    suspend fun forgotPassword(email: String) = apiService.forgotPassword(email)

    suspend fun resetPassword(email: String, token: String, pass: String, passConfirm: String) =
        apiService.resetPassword(email, token, pass, passConfirm)

    suspend fun changePassword(token: String, currentPass: String, newPass: String, newPassConfirm: String) =
        apiService.changePassword("Bearer $token", currentPass, newPass, newPassConfirm)

    suspend fun toggleFollow(token: String, targetId: String) =
        apiService.toggleFollow("Bearer $token", targetId)

    suspend fun checkFollowStatus(token: String, targetId: String) =
        apiService.checkFollowStatus("Bearer $token", targetId)

    suspend fun updateFcmToken(token: String, fcmToken: String) =
        apiService.updateFcmToken("Bearer $token", fcmToken)

    suspend fun broadcastPromo(token: String): ResponseSaka {
        return apiService.broadcastPromo("Bearer $token")
    }

    suspend fun getSellerOrders(token: String): List<OrderItem> {
        return apiService.getSellerOrders("Bearer $token")
    }

    suspend fun updateOrderStatus(token: String, orderId: Int, status: String): ResponseSaka {
        return apiService.updateOrderStatus("Bearer $token", orderId, status)
    }

    suspend fun updateStoreLocation(token: String, address: String, lat: Double, lng: Double): ResponseStore {
        return apiService.updateStoreLocation(token, address, lat, lng)
    }

    suspend fun createTransaction(
        token: String,
        sakaId: Int,
        quantity: Int,
        paymentMethod: String,
        fullAddress: String,
        subtotal: Int,
        totalAmount: Int,
        shippingMethod: String,
        lat: Double?,
        lng: Double?
    ): TransactionResponse {
        return apiService.createTransaction(
            "Bearer $token",
            sakaId,
            quantity,
            paymentMethod,
            fullAddress,
            subtotal,
            totalAmount,
            shippingMethod,
            lat,
            lng
        )
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