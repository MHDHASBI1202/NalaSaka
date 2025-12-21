package com.example.nalasaka.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nalasaka.ui.screen.addsaka.AddSakaScreen
import com.example.nalasaka.ui.screen.detail.DetailScreen
import com.example.nalasaka.ui.screen.home.HomeScreen
import com.example.nalasaka.ui.screen.login.LoginScreen
import com.example.nalasaka.ui.screen.produk.ProductScreen
import com.example.nalasaka.ui.screen.register.RegisterScreen
import com.example.nalasaka.ui.screen.welcome.WelcomeScreen
import com.example.nalasaka.ui.screen.profile.ProfileScreen
import com.example.nalasaka.ui.screen.profile.EditProfileScreen
import com.example.nalasaka.ui.screen.history.TransactionHistoryScreen
import com.example.nalasaka.ui.screen.profile.VerifySellerScreen
import com.example.nalasaka.ui.screen.seller.SellerDashboardScreen // Import ini
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nalasaka.ui.screen.seller.SellerInventoryScreen
import com.example.nalasaka.ui.viewmodel.AuthViewModel

@Composable
fun SakaNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    factory: ViewModelFactory,
) {
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Welcome.route
    ) {
        // 1. WELCOME SCREEN
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // 2. LOGIN SCREEN
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        // 3. REGISTER SCREEN
        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        // 4. HOME SCREEN
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                viewModel = viewModel(factory = factory)
            )
        }

        // --- PRODUK SCREEN ---
        composable(Screen.Produk.route) {
            ProductScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        // 5. ADD SAKA SCREEN (Diakses dari Dashboard Seller sekarang)
        composable(Screen.AddSaka.route) {
            AddSakaScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        // 6. DETAIL SCREEN
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("sakaId") { type = NavType.StringType })
        ) {
            val sakaId = it.arguments?.getString("sakaId") ?: return@composable
            DetailScreen(
                sakaId = sakaId,
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        // 7. PROFILE SCREEN
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        // 8. EDIT PROFILE SCREEN
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }

        // VERIFY SELLER SCREEN
        composable(Screen.VerifySeller.route) {
            VerifySellerScreen(navController = navController)
        }

        // VERIFY SELLER SCREEN (Aktivasi Toko)
        composable(Screen.VerifySeller.route) {
            VerifySellerScreen(navController = navController)
        }

        // [NEW] UPLOAD CERTIFICATION SCREEN
        composable(Screen.UploadCertification.route) {
            // Import screen ini manual jika merah: com.example.nalasaka.ui.screen.profile.UploadCertificationScreen
            com.example.nalasaka.ui.screen.profile.UploadCertificationScreen(navController = navController)
        }

        // --- NEW: DASHBOARD SELLER ---
        composable(Screen.SellerDashboard.route) {
            SellerDashboardScreen(
                navController = navController,
                authViewModel = authViewModel // Reuse auth viewmodel untuk data user
            )
        }

        // NEW: Rute Inventory/Stok Seller
        composable(Screen.SellerInventory.route) {
            SellerInventoryScreen(navController = navController)
        }

        // NEW: WISHLIST SCREEN
        composable(Screen.Wishlist.route) {
            // Pastikan import WishlistScreen sudah benar
            com.example.nalasaka.ui.screen.wishlist.WishlistScreen(navController = navController)
        }

        composable(Screen.Cart.route) {
            // Pastikan import com.example.nalasaka.ui.screen.cart.CartScreen
            com.example.nalasaka.ui.screen.cart.CartScreen(navController = navController)
        }

        // FORGOT PASSWORD
        composable(Screen.ForgotPassword.route) {
            com.example.nalasaka.ui.screen.login.ForgotPasswordScreen(navController = navController)
        }

        // CHANGE PASSWORD
        composable(Screen.ChangePassword.route) {
            com.example.nalasaka.ui.screen.profile.ChangePasswordScreen(navController = navController)
        }

        // 9. TRANSACTION HISTORY SCREEN
        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }
    }
}