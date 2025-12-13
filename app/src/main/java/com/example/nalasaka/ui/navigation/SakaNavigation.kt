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
import com.example.nalasaka.ui.screen.profile.VerifySellerScreen // NEW: Import VerifySellerScreen
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
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

        // 4. HOME SCREEN (MODUL PRODUK & PEMASARAN)
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                viewModel = viewModel(factory = factory)
            )
        }

        // --- TAMBAHAN: PRODUK SCREEN ---
        composable(Screen.Produk.route) {
            ProductScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        // 5. ADD SAKA SCREEN (UPLOAD FOTO BARANG)
        composable(Screen.AddSaka.route) {
            AddSakaScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        // 6. DETAIL SCREEN (Membutuhkan ID Produk)
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

        // --- RUTE PROFIL & PENJUAL ---

        // 7. PROFILE SCREEN (Dipertahankan versi dengan ViewModel)
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

        // NEW: VERIFY SELLER SCREEN
        composable(Screen.VerifySeller.route) {
            VerifySellerScreen(navController = navController)
        }

        // 9. TRANSACTION HISTORY SCREEN (Modul Transaksi & Logistik)
        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }
    }
}