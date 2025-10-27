package com.example.nalasaka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.nalasaka.ui.navigation.SakaNavigation
import com.example.nalasaka.ui.theme.NalaSakaTheme
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

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
    // Inisialisasi NavController
    val navController = rememberNavController()
    // Mendapatkan instance ViewModelFactory
    val viewModelFactory = ViewModelFactory.getInstance(navController.context)

    // Memanggil Navigasi Utama Aplikasi
    SakaNavigation(
        navController = navController,
        factory = viewModelFactory
    )
}