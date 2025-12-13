package com.example.nalasaka.data.remote.retrofit

import com.example.nalasaka.data.remote.response.AllSakaResponse
import com.example.nalasaka.data.remote.response.CheckoutResponse
import com.example.nalasaka.data.remote.response.DetailSakaResponse
import com.example.nalasaka.data.remote.response.ProfileResponse
import com.example.nalasaka.data.remote.response.ResponseSaka
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
        // --- PERUBAHAN BARU: Tambah no_hp dan alamat ---
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
        // Parameter opsional untuk Pagination
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
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody // Price as RequestBody
    ): ResponseSaka


    // --- MODUL TRANSAKSI & LOGISTIK ---

    // 1. Ambil Riwayat Transaksi
    @GET("transactions")
    suspend fun getTransactionHistory(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String
    ): TransactionHistoryResponse

    // 2. Checkout / Pesan Barang
    @FormUrlEncoded
    @POST("transactions")
    suspend fun checkoutTransaction(
        @Header("Authorization") token: String,
        @Field("user_id") userId: String,
        @Field("saka_id") sakaId: String,
        @Field("quantity") quantity: Int
    ): CheckoutResponse

    // --- TAMBAHAN UNTUK MODUL PROFIL ---
    @GET("user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): ProfileResponse

    // Endpoint untuk update profil (name, phone_number, address, store_name)
    @FormUrlEncoded
    @PATCH("user/profile")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Field("name") name: String,
        @Field("phone_number") phoneNumber: String,
        @Field("address") address: String,
        @Field("store_name") storeName: String? = null // FIX: Menambahkan store_name
    ): ProfileResponse

    // NEW: Endpoint untuk mengaktifkan mode penjual
    @FormUrlEncoded
    @POST("user/activate-seller")
    suspend fun activateSellerMode(
        @Header("Authorization") token: String,
        @Field("store_name") storeName: String
    ): ProfileResponse
}