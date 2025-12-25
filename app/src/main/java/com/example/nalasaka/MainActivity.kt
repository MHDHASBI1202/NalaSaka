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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.nalasaka.ui.components.SakaBottomBar
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.di.Injection
import com.google.firebase.messaging.FirebaseMessaging
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val repository = Injection.provideRepository(context)
            val userModel by repository.getUser().collectAsState(initial = null)

            // Indentasi diperbaiki: Sejajar dengan deklarasi variabel di atasnya
            LaunchedEffect(userModel?.isLogin) {
                if (userModel?.isLogin == true) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            lifecycleScope.launch {
                                try {
                                    repository.updateFcmToken(userModel!!.token, token)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                }
            }

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

    val userModel by repository.getUser().collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false)
    )
    val userRole = userModel.role

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Profile.route,
        Screen.Produk.route,
        Screen.TransactionHistory.route,
        Screen.SellerDashboard.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
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