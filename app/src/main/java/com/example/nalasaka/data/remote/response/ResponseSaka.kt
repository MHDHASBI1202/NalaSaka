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