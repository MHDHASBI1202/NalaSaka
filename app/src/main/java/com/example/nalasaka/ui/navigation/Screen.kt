package com.example.nalasaka.ui.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")

    object Home : Screen("home")
    object AddSaka : Screen("addsaka")

    object Produk : Screen("produk_nav")

    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object VerifySeller : Screen("verify_seller")

    object UploadCertification : Screen("upload_certification")

    object SellerDashboard : Screen("seller_dashboard")
    object SellerInventory : Screen("seller_inventory")

    object SellerOrders : Screen("seller_orders_list")

    object Detail : Screen("detail/{sakaId}") {
        fun createRoute(sakaId: String) = "detail/$sakaId"
    }

    object Wishlist : Screen("wishlist")

    object Cart : Screen("cart")

    object TransactionHistory : Screen("transaction_history")

    object ForgotPassword : Screen("forgot_password")

    object ChangePassword : Screen("change_password")
}