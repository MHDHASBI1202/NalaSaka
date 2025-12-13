package com.example.nalasaka.ui.navigation

sealed class Screen(val route: String) {
    // Rute Autentikasi dan Pembuka
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")

    // Rute Modul Produk & Pemasaran (Inti Aplikasi)
    object Home : Screen("home")
    object AddSaka : Screen("addsaka") // Untuk Upload Foto Barang

    // --- TAMBAHAN UNTUK MODUL PRODUK ---
    object Produk : Screen("produk_nav")

    // --- TAMBAHAN UNTUK MODUL PROFIL ---
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")

    // Rute Detail Produk (Memerlukan argumen ID)
    object Detail : Screen("detail/{sakaId}") {
        fun createRoute(sakaId: String) = "detail/$sakaId"
    }

    // Rute untuk Riwayat Transaksi
    object TransactionHistory : Screen("transaction_history")

    // Anda bisa tambahkan rute untuk modul Reputasi & Analisis di sini,
    // misalnya: object Dashboard: Screen("dashboard")
}