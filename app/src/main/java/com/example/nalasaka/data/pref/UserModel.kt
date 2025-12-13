package com.example.nalasaka.data.pref

data class UserModel(
    val userId: String,
    val name: String,
    val token: String,
    val isLogin: Boolean = false, // Flag untuk status login
    val role: String = "customer" // NEW: Tambahkan role dengan default 'customer'
)