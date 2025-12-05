package com.example.nalasaka.data.remote.response

import com.google.gson.annotations.SerializedName

// General Response for operations like Register, Login, Add Saka
data class ResponseSaka(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    // Digunakan khusus untuk respons Login
    @field:SerializedName("loginResult")
    val loginResult: LoginResult? = null
)

// Data class untuk hasil Login
data class LoginResult(
    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("token")
    val token: String
)

// Data class untuk satu item produk Tani
data class SakaItem(
    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("photoUrl")
    val photoUrl: String,

    @field:SerializedName("description")
    val description: String,

    // Asumsi harga adalah integer
    @field:SerializedName("price")
    val price: Int
)

// Response untuk Mendapatkan Semua Produk
data class AllSakaResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("listSaka")
    val listSaka: List<SakaItem>
)

// Response untuk Mendapatkan Detail Produk
data class DetailSakaResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("saka")
    val saka: SakaItem
)
// Model untuk data Tracking (Lokasi & Resi)
data class TrackingData(
    @field:SerializedName("location")
    val location: String,

    @field:SerializedName("resi")
    val resi: String
)

// Model untuk satu item Riwayat Transaksi
data class TransactionItem(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("productName")
    val productName: String,

    @field:SerializedName("productImage")
    val productImage: String,

    @field:SerializedName("price")
    val price: Int,

    @field:SerializedName("status")
    val status: String, // PENDING, DIPROSES, DIKIRIM, dll

    @field:SerializedName("date")
    val date: String,

    @field:SerializedName("tracking")
    val tracking: TrackingData
)

// Response untuk GET /api/transactions
data class TransactionHistoryResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("history")
    val history: List<TransactionItem>
)

// Response Sederhana untuk Checkout (POST)
data class CheckoutResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("transaction_id")
    val transactionId: Int? = null
)
// Data class untuk Detail Profil Pengguna
data class ProfileData(
    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("email")
    val email: String,

    @field:SerializedName("photoUrl")
    val photoUrl: String? = null, // Opsional

    @field:SerializedName("phoneNumber")
    val phoneNumber: String? = null, // Data tambahan

    @field:SerializedName("address")
    val address: String? = null // Data tambahan
)

// Response untuk Mendapatkan Detail Profil Pengguna
data class ProfileResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("user")
    val user: ProfileData
)