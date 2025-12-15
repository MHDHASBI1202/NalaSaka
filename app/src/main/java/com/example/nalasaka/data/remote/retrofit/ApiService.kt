package com.example.nalasaka.data.remote.retrofit

import com.example.nalasaka.data.remote.response.AllSakaResponse
import com.example.nalasaka.data.remote.response.CheckoutResponse
import com.example.nalasaka.data.remote.response.DetailSakaResponse
import com.example.nalasaka.data.remote.response.ProfileResponse
import com.example.nalasaka.data.remote.response.ResponseSaka
import com.example.nalasaka.data.remote.response.ReviewApiResponse
import com.example.nalasaka.data.remote.response.SellerStatsResponse
import com.example.nalasaka.data.remote.response.TransactionHistoryResponse
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

    @FormUrlEncoded
    @POST("transactions")
    suspend fun checkoutTransaction(
        @Header("Authorization") token: String,
        @Field("user_id") userId: String,
        @Field("saka_id") sakaId: String,
        @Field("quantity") quantity: Int
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
}
