package com.example.nalasaka.data.remote.response

import com.google.gson.annotations.SerializedName

data class ResponseSaka(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    // Digunakan khusus untuk respons Login
    @field:SerializedName("loginResult")
    val loginResult: LoginResult? = null
)

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

    @field:SerializedName("discountPrice")
    val discountPrice: Int? = null,

    @field:SerializedName("stock")
    val stock: Int = 0,

    @field:SerializedName("sellerId")
    val sellerId: String? = null,

    @field:SerializedName("isSellerVerified")
    val isSellerVerified: Boolean = false,

    @field:SerializedName("category")
    val category: String = "Umum",

    @field:SerializedName("sellerName")
    val sellerName: String = "Penjual",

    @field:SerializedName("sellerPhotoUrl")
    val sellerPhotoUrl: String? = null
)

data class AllSakaResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("listSaka")
    val listSaka: List<SakaItem>
)

data class DetailSakaResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("saka")
    val saka: SakaItem
)

data class TrackingData(
    @field:SerializedName("location")
    val location: String,

    @field:SerializedName("resi")
    val resi: String
)

data class TransactionItem(
    @field:SerializedName("id")
    val id: String,

    @field:SerializedName("sakaId")
    val sakaId: String,

    @field:SerializedName("productName")
    val productName: String,

    @field:SerializedName("productImage")
    val productImage: String,

    @field:SerializedName("price")
    val price: Int,

    @field:SerializedName("status")
    val status: String,

    @field:SerializedName("date")
    val date: String,

    @field:SerializedName("tracking")
    val tracking: TrackingData,

    @field:SerializedName("shipping_method")
    val shippingMethod: String?,

    @field:SerializedName("pickup_code")
    val pickupCode: String?,

    @field:SerializedName("store_name")
    val storeName: String?,

    @field:SerializedName("store_address")
    val storeAddress: String?,

    @field:SerializedName("latitude")
    val latitude: Double?,

    @field:SerializedName("longitude")
    val longitude: Double?
)

data class TransactionHistoryResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("history")
    val history: List<TransactionItem>
)

data class CheckoutResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("transaction_id")
    val transactionId: Int? = null
)

data class TransactionResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("transaction")
    val transaction: Any? = null
)

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

    @field:SerializedName("store_address")
    val storeAddress: String? = null,

    @field:SerializedName("followersCount")
    val followersCount: Int = 0,

    @field:SerializedName("followingCount")
    val followingCount: Int = 0,

    @field:SerializedName("verificationStatus")
    val verificationStatus: String = "none"
)

data class FollowResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("isFollowing")
    val isFollowing: Boolean
)

data class ProfileResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("user")
    val user: ProfileData
)

data class SellerStats(
    @field:SerializedName("revenue")
    val revenue: Int,

    @field:SerializedName("sold")
    val sold: Int,

    @field:SerializedName("product_count")
    val productCount: Int,

    @field:SerializedName("daily_sales")
    val dailySales: List<DailySalesItem> = emptyList(),

    @field:SerializedName("product_performance")
    val productPerformance: List<ProductSalesStat> = emptyList()
)

data class DailySalesItem(
    @field:SerializedName("day")
    val day: String,
    @field:SerializedName("amount")
    val amount: Int
)

data class ProductSalesStat(
    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("image_url")
    val imageUrl: String,

    @field:SerializedName("sold_qty")
    val soldQty: Int,

    @field:SerializedName("total_revenue")
    val totalRevenue: Int
)

data class SellerStatsResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("stats")
    val stats: SellerStats
)

data class ReviewItem(
    @field:SerializedName("id")
    val id: String,

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

data class WishlistResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("isWishlist")
    val isWishlist: Boolean
)

data class CartResponse(
    @field:SerializedName("error") val error: Boolean,
    @field:SerializedName("message") val message: String,
    @field:SerializedName("data") val data: List<CartItem>
)

data class CartItem(
    @field:SerializedName("cart_id") val cartId: Int,
    @field:SerializedName("saka_id") val sakaId: String,
    @field:SerializedName("name") val name: String,
    @field:SerializedName("price") val price: Int,
    @field:SerializedName("photo_url") val photoUrl: String,
    @field:SerializedName("quantity") val quantity: Int,
    @field:SerializedName("stock_available") val stockAvailable: Int,
    @field:SerializedName("store_name") val storeName: String? = null,
    @field:SerializedName("storeAddress") val storeAddress: String? = null,
    @field:SerializedName("latitude") val latitude: Double? = null,
    @field:SerializedName("longitude") val longitude: Double? = null

)

data class OrderItem(
    @field:SerializedName("id") val id: Int,
    @field:SerializedName("product_name") val productName: String,
    @field:SerializedName("quantity") val quantity: Int,
    @field:SerializedName("status") val status: String,
    @field:SerializedName("buyer_name") val buyerName: String,
    @field:SerializedName("shipping_method") val shippingMethod: String,
    @field:SerializedName("resiNumber") val resiNumber: String? = null,
    @field:SerializedName("full_address") val fullAddress: String,
    @field:SerializedName("totalPrice") val totalPrice: Int,
    @field:SerializedName("latitude") val latitude: Double? = null,
    @field:SerializedName("longitude") val longitude: Double? = null,
    @field:SerializedName("pickupCode") val pickupCode: String? = null
)

data class ResponseStore(
    @field:SerializedName("error") val error: Boolean,
    @field:SerializedName("message") val message: String
)