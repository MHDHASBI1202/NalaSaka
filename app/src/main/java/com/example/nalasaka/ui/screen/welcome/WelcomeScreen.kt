package com.example.nalasaka.ui.screen.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nalasaka.R
import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.di.Injection
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.navigation.Screen

@Composable
fun WelcomeScreen(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    // Ambil sesi user dari repository
    val userFlow = Injection.provideRepository(context).getUser()
    val userModel by userFlow.collectAsState(initial = UserModel("", "", "", false))

    // Periksa status login dan navigasi
    LaunchedEffect(userModel.isLogin) {
        if (userModel.isLogin) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ganti dengan logo NalaSaka Anda.
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "NalaSaka Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Selamat Datang di NalaSaka",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Tombol Login (Sekarang Menggunakan Secondary: Deep Moss)
        PrimaryButton(
            text = "LOGIN",
            onClick = { navController.navigate(Screen.Login.route) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary // Deep Moss
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Tombol Register (Menggunakan Secondary: Deep Moss)
        PrimaryButton(
            text = "DAFTAR (REGISTER)",
            onClick = { navController.navigate(Screen.Register.route) },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary // Deep Moss
            )
        )
    }
}