package com.example.nalasaka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.nalasaka.ui.navigation.SakaNavigation
import com.example.nalasaka.ui.theme.NalaSakaTheme
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState // Import ini
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.nalasaka.ui.components.SakaBottomBar
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.di.Injection // Import ini

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NalaSakaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NalaSakaApp()
                }
            }
        }
    }
}

@Composable
fun NalaSakaApp() {
    val navController = rememberNavController()
    val context = navController.context
    val viewModelFactory = ViewModelFactory.getInstance(context)
    val repository = Injection.provideRepository(context)

    // --- LOGIKA UTAMA: Ambil Role User secara Real-time ---
    val userModel by repository.getUser().collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false)
    )
    val userRole = userModel.role
    // -----------------------------------------------------

    // Tentukan apakah BottomBar harus ditampilkan
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tambahkan Screen.SellerDashboard.route ke dalam list showBottomBar
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Profile.route,
        Screen.Produk.route,
        Screen.TransactionHistory.route,
        Screen.SellerDashboard.route // Bottom bar muncul di dashboard seller
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                // Pass userRole ke BottomBar
                SakaBottomBar(navController = navController, userRole = userRole)
            }
        }
    ) { paddingValues ->
        SakaNavigation(
            modifier = Modifier.padding(paddingValues),
            navController = navController,
            factory = viewModelFactory
        )
    }
}