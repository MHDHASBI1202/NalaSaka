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
import com.example.nalasaka.ui.screen.seller.SellerDashboardScreen
import com.example.nalasaka.ui.screen.checkout.CheckoutScreen
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nalasaka.ui.screen.seller.SellerInventoryScreen
import com.example.nalasaka.ui.screen.seller.SellerOrderScreen
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
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                viewModel = viewModel(factory = factory)
            )
        }

        composable(Screen.Produk.route) {
            ProductScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        composable(Screen.AddSaka.route) {
            AddSakaScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

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

        composable(Screen.Profile.route) {
            ProfileScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }

        composable(Screen.VerifySeller.route) {
            VerifySellerScreen(navController = navController)
        }

        composable(Screen.VerifySeller.route) {
            VerifySellerScreen(navController = navController)
        }

        composable(Screen.UploadCertification.route) {
            com.example.nalasaka.ui.screen.profile.UploadCertificationScreen(navController = navController)
        }

        composable(Screen.SellerDashboard.route) {
            SellerDashboardScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.SellerInventory.route) {
            SellerInventoryScreen(navController = navController)
        }

        composable(Screen.Wishlist.route) {
            com.example.nalasaka.ui.screen.wishlist.WishlistScreen(navController = navController)
        }

        composable(Screen.Cart.route) {
            com.example.nalasaka.ui.screen.cart.CartScreen(navController = navController)
        }

        composable(Screen.ForgotPassword.route) {
            com.example.nalasaka.ui.screen.login.ForgotPasswordScreen(navController = navController)
        }

        composable(Screen.ChangePassword.route) {
            com.example.nalasaka.ui.screen.profile.ChangePasswordScreen(navController = navController)
        }

        composable(
            route = "checkout/{subtotal}",
            arguments = listOf(navArgument("subtotal") { type = NavType.IntType })
        ) { backStackEntry ->
            val subtotal = backStackEntry.arguments?.getInt("subtotal") ?: 0
            CheckoutScreen(
                navController = navController,
                subtotal = subtotal
            )
        }

        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(
                navController = navController,
                viewModel = viewModel(factory = factory)
            )
        }
        composable("seller_orders_list") {
            SellerOrderScreen(navController = navController)
        }

    }
}