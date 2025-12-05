package com.example.nalasaka.ui.screen.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background // Import ini
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box // Import ini
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
import androidx.compose.ui.graphics.Brush // Import ini
import androidx.compose.ui.graphics.Color // Import ini
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
import com.example.nalasaka.ui.theme.WelcomeGradientEnd // Import ini
import com.example.nalasaka.ui.theme.WelcomeGradientStart // Import ini

@Composable
fun WelcomeScreen(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    // 1. Tentukan gradient background
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(WelcomeGradientStart, WelcomeGradientEnd)
    )

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

    // Ganti Column luar dengan Box untuk menampung background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush) // Terapkan Gradient
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Konten visual (sesuai gambar: Gambar Makanan)
            // Hamba menggunakan gambar bawaan Android sebagai placeholder untuk mockup Yang Mulia
            Image(
                // Ganti dengan aset gambar yang Yang Mulia gunakan di Launch.png jika sudah ada
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "NalaSaka Image",
                modifier = Modifier.size(160.dp) // Ukuran sedikit diperbesar
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nama Aplikasi (Warna Putih agar kontras)
            Text(
                text = "NalaSaka",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle (Warna Putih agar kontras)
            Text(
                text = "Find your fresh food",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            // Pindahkan Tombol ke bagian bawah, atau ubah menjadi Splash/Launch sejati
            // Untuk Launch Screen yang murni, tombol login/daftar ini harus dihilangkan
            // dan dipindahkan ke screen berikutnya (Login/Register).
            // Namun, karena ini masih berfungsi sebagai "Welcome Screen" yang berisi tombol,
            // kita pindahkan ke bawah agar terpisah dari logo.

            Spacer(modifier = Modifier.height(80.dp)) // Jarak yang lebih jauh

            // Tombol Login (Menggunakan Primary: Burnt Orangeish)
            PrimaryButton(
                text = "LOGIN",
                onClick = { navController.navigate(Screen.Login.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Register (Menggunakan Secondary: Deep Moss)
            PrimaryButton(
                text = "DAFTAR (REGISTER)",
                onClick = { navController.navigate(Screen.Register.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary, // Deep Moss
                    contentColor = MaterialTheme.colorScheme.onSecondary // Biasanya Putih
                )
            )
        }
    }
}