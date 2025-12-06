package com.example.nalasaka.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.nalasaka.ui.screen.addsaka.AddSakaScreen
import com.example.nalasaka.ui.screen.detail.DetailScreen
import com.example.nalasaka.ui.screen.history.TransactionHistoryScreen
import androidx.compose.ui.platform.LocalContext
import com.example.nalasaka.ui.screen.home.HomeScreen
import com.example.nalasaka.ui.screen.login.LoginScreen
import com.example.nalasaka.ui.screen.produk.ProductScreen
import com.example.nalasaka.ui.screen.profile.ProfileScreen
import com.example.nalasaka.ui.screen.register.RegisterScreen
import com.example.nalasaka.ui.screen.seller.SellerVerificationScreen
import com.example.nalasaka.ui.screen.welcome.WelcomeScreen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

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
        startDestination = Screen.Welcome.route // Titik mulai
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
                navController = navController
            )
        }

        // 3. REGISTER SCREEN
        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController
            )
        }

        // 4. HOME SCREEN
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // --- TAMBAHAN: PRODUK SCREEN ---
        composable(Screen.Produk.route) {
            ProductScreen(
                navController = navController
            )
        }

        // 5. ADD SAKA SCREEN (UPLOAD FOTO BARANG)
        composable(Screen.AddSaka.route) {
            AddSakaScreen(
                navController = navController
            )
        }

        // --- RUTE PROFIL & PENJUAL ---
        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                // Parameter navigasi baru yang HARUS diterima oleh ProfileScreen
                navigateToSellerVerification = { navController.navigate(Screen.SellerVerification.route) }
            )
        }

        // Rute Baru: Seller Verification Screen
        composable(Screen.SellerVerification.route) {
            SellerVerificationScreen(
                // Parameter navigasi yang HARUS diterima oleh SellerVerificationScreen
                navigateBack = { navController.navigateUp() },
                navigateToHome = {
                    // Navigasi kembali ke Home setelah sukses, membersihkan back stack
                    navController.popBackStack(Screen.Home.route, inclusive = true)
                    navController.navigate(Screen.Home.route)
                }
            )
        }
        // -----------------------------

        // 6. DETAIL SCREEN
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("sakaId") { type = NavType.StringType })
        ) {
            val sakaId = it.arguments?.getString("sakaId") ?: return@composable
            DetailScreen(
                sakaId = sakaId,
                navController = navController
            )
        }

        // 7. TRANSACTION HISTORY SCREEN
        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(
                navController = navController
            )
        }
    }
}