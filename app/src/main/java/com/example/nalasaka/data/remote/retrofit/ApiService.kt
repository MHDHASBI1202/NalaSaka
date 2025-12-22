package com.example.nalasaka.data.remote.retrofit

import com.example.nalasaka.data.remote.response.AllSakaResponse
import com.example.nalasaka.data.remote.response.CartResponse
import com.example.nalasaka.data.remote.response.CheckoutResponse
import com.example.nalasaka.data.remote.response.DetailSakaResponse
import com.example.nalasaka.data.remote.response.FollowResponse
import com.example.nalasaka.data.remote.response.ProfileResponse
import com.example.nalasaka.data.remote.response.ResponseSaka
import com.example.nalasaka.data.remote.response.ReviewApiResponse
import com.example.nalasaka.data.remote.response.SellerStatsResponse
import com.example.nalasaka.data.remote.response.TransactionHistoryResponse
import com.example.nalasaka.data.remote.response.WishlistResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone_number") phoneNumber: String,
        @Field("address") address: String,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConfirmation: String
    ): ResponseSaka

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): ResponseSaka

    @GET("saka")
    suspend fun getAllSaka(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
    ): AllSakaResponse

    @GET("saka/{sakaId}")
    suspend fun getDetailSaka(
        @Header("Authorization") token: String,
        @Path("sakaId") sakaId: String
    ): DetailSakaResponse

    @Multipart
    @POST("saka")
    suspend fun addNewSaka(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("name") name: RequestBody,
        @Part("category") category: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("stock") stock: RequestBody
    ): ResponseSaka

    @FormUrlEncoded
    @PATCH("saka/{id}/stock")
    suspend fun updateStock(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Field("stock") stock: Int
    ): ResponseSaka

    @DELETE("saka/{id}")
    suspend fun deleteSaka(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ResponseSaka

    // --- MODUL TRANSAKSI & LOGISTIK ---

    @GET("transactions")
    suspend fun getTransactionHistory(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String
    ): TransactionHistoryResponse

    // Di Transaction Store (Pesan Ulang)
    @FormUrlEncoded
    @POST("transactions")
    suspend fun checkoutTransaction(
        @Header("Authorization") token: String,
        @Field("user_id") userId: String,
        @Field("saka_id") sakaId: String,
        @Field("quantity") quantity: Int,
        @Field("payment_method") paymentMethod: String // Tambah ini
    ): CheckoutResponse

    // --- MODUL PROFIL ---
    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): ProfileResponse

    @FormUrlEncoded
    @PATCH("user/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Field("name") name: String,
        @Field("phone_number") phoneNumber: String,
        @Field("address") address: String,
        @Field("store_name") storeName: String? = null
    ): ProfileResponse

    @FormUrlEncoded
    @POST("user/activate-seller")
    suspend fun activateSellerMode(
        @Header("Authorization") token: String,
        @Field("store_name") storeName: String
    ): ProfileResponse

    @Multipart
    @POST("user/upload-certification")
    suspend fun uploadCertification(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): ProfileResponse

    @GET("saka/my-products")
    suspend fun getMyProducts(
        @Header("Authorization") token: String
    ): AllSakaResponse

    @GET("seller/stats")
    suspend fun getSellerStats(
        @Header("Authorization") token: String
    ): SellerStatsResponse

    // --- MODUL REPUTASI & ANALISIS (NEW) ---

    // 1. Ambil Review berdasarkan Produk
    @GET("saka/{sakaId}/reviews")
    suspend fun getProductReviews(
        @Header("Authorization") token: String,
        @Path("sakaId") sakaId: String
    ): ReviewApiResponse

    // 2. Kirim Review Baru (Dengan Foto Opsional)
    @Multipart
    @POST("reviews")
    suspend fun addReview(
        @Header("Authorization") token: String,
        @Part("saka_id") sakaId: RequestBody,
        @Part("rating") rating: RequestBody,
        @Part("comment") comment: RequestBody,
        @Part file: MultipartBody.Part? = null // Foto opsional
    ): ResponseSaka // Kita pakai ResponseSaka umum karena format suksesnya mirip

    // --- MODUL WISHLIST ---
    @FormUrlEncoded
    @POST("wishlist/toggle")
    suspend fun toggleWishlist(
        @Header("Authorization") token: String,
        @Field("saka_id") sakaId: String
    ): WishlistResponse

    @GET("wishlist/check/{sakaId}")
    suspend fun checkWishlist(
        @Header("Authorization") token: String,
        @Path("sakaId") sakaId: String
    ): WishlistResponse

    @GET("wishlist")
    suspend fun getMyWishlist(
        @Header("Authorization") token: String
    ): AllSakaResponse

    // --- CART ---
    @GET("cart")
    suspend fun getCart(@Header("Authorization") token: String): CartResponse

    @FormUrlEncoded
    @POST("cart")
    suspend fun addToCart(
        @Header("Authorization") token: String,
        @Field("saka_id") sakaId: String,
        @Field("quantity") quantity: Int
    ): ResponseSaka

    @FormUrlEncoded
    @PATCH("cart/{id}")
    suspend fun updateCartQty(
        @Header("Authorization") token: String,
        @Path("id") cartId: Int,
        @Field("quantity") quantity: Int
    ): ResponseSaka

    @DELETE("cart/{id}")
    suspend fun deleteCartItem(
        @Header("Authorization") token: String,
        @Path("id") cartId: Int
    ): ResponseSaka

    @FormUrlEncoded
    @POST("cart/checkout")
    suspend fun checkoutCart(
        @Header("Authorization") token: String,
        @Field("payment_method") paymentMethod: String // Tambah ini
    ): ResponseSaka

    // 1. Forgot Password (Request Token)
    @FormUrlEncoded
    @POST("password/forgot")
    suspend fun forgotPassword(
        @Field("email") email: String
    ): ResponseSaka

    // 2. Reset Password (Pakai Token)
    @FormUrlEncoded
    @POST("password/reset")
    suspend fun resetPassword(
        @Field("email") email: String,
        @Field("token") token: String,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConfirmation: String
    ): ResponseSaka

    // 3. Change Password (Di Profil)
    @FormUrlEncoded
    @POST("user/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Field("current_password") currentPass: String,
        @Field("new_password") newPass: String,
        @Field("new_password_confirmation") newPassConfirm: String
    ): ResponseSaka

    @FormUrlEncoded
    @POST("user/follow")
    suspend fun toggleFollow(
        @Header("Authorization") token: String,
        @Field("target_user_id") targetId: String
    ): FollowResponse

    @GET("user/follow/check/{targetId}")
    suspend fun checkFollowStatus(
        @Header("Authorization") token: String,
        @Path("targetId") targetId: String
    ): FollowResponse

    @FormUrlEncoded
    @POST("user/fcm-token")
    suspend fun updateFcmToken(
        @Header("Authorization") token: String,
        @Field("fcm_token") fcmToken: String
    ): ResponseSaka

    @POST("seller/broadcast")
    suspend fun broadcastPromo(
        @Header("Authorization") token: String
    ): ResponseSaka
}
