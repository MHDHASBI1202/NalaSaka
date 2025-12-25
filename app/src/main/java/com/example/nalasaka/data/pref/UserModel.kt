package com.example.nalasaka.data.pref

data class UserModel(
    val userId: String,
    val name: String,
    val token: String,
    val isLogin: Boolean = false,
    val role: String = "customer",
    val isPromoClaimed: Boolean = false,
    val isPromoUsed: Boolean = false
)