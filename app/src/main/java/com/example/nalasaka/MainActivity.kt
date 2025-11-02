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
import androidx.compose.material3.Scaffold // Import ini
import androidx.compose.runtime.getValue // Import ini
import androidx.navigation.compose.currentBackStackEntryAsState // Import ini
import com.example.nalasaka.ui.components.SakaBottomBar // Import ini
import com.example.nalasaka.ui.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mengizinkan konten menjangkau di bawah bilah sistem
        enableEdgeToEdge()
        setContent {
            // Menerapkan tema NalaSaka
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
    val viewModelFactory = ViewModelFactory.getInstance(navController.context)

    // Tentukan apakah BottomBar harus ditampilkan (di luar Welcome, Login, Register)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(Screen.Home.route, Screen.Profile.route)
    // Tambahkan rute Bottom Bar lainnya di sini jika sudah dibuat

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                SakaBottomBar(navController = navController)
            }
        }
    ) { paddingValues ->
        // Memanggil Navigasi Utama Aplikasi
        SakaNavigation(
            modifier = Modifier.padding(paddingValues), // Penting: berikan paddingValues ke NavHost
            navController = navController,
            factory = viewModelFactory
        )
    }
}