package com.example.nalasaka.ui.navigation

sealed class Screen(val route: String) {

    // Rute Autentikasi dan Pembuka
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")

    // Rute Modul Produk & Pemasaran
    object Home : Screen("home")
    object Produk : Screen("produk_nav") // Rute untuk navigasi ke daftar produk
    object AddSaka : Screen("addsaka") // Untuk Upload Foto Barang

    // Rute Detail Produk (Memerlukan argumen ID, menggunakan konvensi String dari sumber asli)
    object Detail : Screen("detail/{sakaId}") {
        fun createRoute(sakaId: String) = "detail/$sakaId"
    }

    // Rute Modul Profil & Penjual
    object Profile : Screen("profile")

    // Rute Baru: Verifikasi Penjual (Sesuai permintaan)
    object SellerVerification : Screen("seller_verification")

    // Rute untuk Riwayat Transaksi
    object TransactionHistory : Screen("transaction_history")

    // Catatan: Jika ada rute 'History' yang berbeda, mohon berikan detailnya.
}