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
    val token: String,

    @field:SerializedName("role")
    val role: String = "customer"
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

    @field:SerializedName("price")
    val price: Int,

    @field:SerializedName("stock")
    val stock: Int = 0,

    @field:SerializedName("sellerId")
    val sellerId: String? = null,

    // [NEW] Tambahkan field ini (Nanti backend menyusul)
    @field:SerializedName("isSellerVerified")
    val isSellerVerified: Boolean = false,

    @field:SerializedName("category")
    val category: String = "Umum"
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
    val photoUrl: String? = null,

    @field:SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("role")
    val role: String = "customer",

    @field:SerializedName("storeName")
    val storeName: String? = null,

    // [NEW] Tambahkan ini
    @field:SerializedName("verificationStatus")
    val verificationStatus: String = "none"
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

// Model Data Stats (Seller Dashboard)
data class SellerStats(
    @field:SerializedName("revenue")
    val revenue: Int,

    @field:SerializedName("sold")
    val sold: Int,

    @field:SerializedName("product_count")
    val productCount: Int
)

// Response Stats
data class SellerStatsResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("stats")
    val stats: SellerStats
)

// --- BAGIAN BARU: REVIEW & RATING (UPDATED) ---

data class ReviewItem(
    @field:SerializedName("id")
    val id: String,

    // [BARU] Field ini Wajib ada untuk membedakan ulasan saya vs orang lain
    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("userName")
    val userName: String,

    @field:SerializedName("userPhoto")
    val userPhoto: String,

    @field:SerializedName("rating")
    val rating: Int,

    @field:SerializedName("comment")
    val comment: String,

    @field:SerializedName("imageUrl")
    val imageUrl: String? = null,

    @field:SerializedName("date")
    val date: String
)

data class ReviewData(
    @field:SerializedName("averageRating")
    val averageRating: Double,

    @field:SerializedName("totalReviews")
    val totalReviews: Int,

    @field:SerializedName("reviews")
    val reviews: List<ReviewItem>
)

data class ReviewApiResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("data")
    val data: ReviewData
)