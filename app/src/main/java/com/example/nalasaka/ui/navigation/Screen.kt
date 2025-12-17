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
    object VerifySeller : Screen("verify_seller")

    // [NEW] Screen untuk upload sertifikat
    object UploadCertification : Screen("upload_certification")

    // --- NEW: DASHBOARD SELLER (Pengganti Promo) ---
    object SellerDashboard : Screen("seller_dashboard")

    object SellerInventory : Screen("seller_inventory")

    // Rute Detail Produk (Memerlukan argumen ID)
    object Detail : Screen("detail/{sakaId}") {
        fun createRoute(sakaId: String) = "detail/$sakaId"
    }

    // Rute Wishlist
    object Wishlist : Screen("wishlist")

    // Rute untuk Riwayat Transaksi
    object TransactionHistory : Screen("transaction_history")
}