package com.example.nalasaka.ui.screen.profile

import androidx.compose.foundation.Image // Import baru
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.ProfileViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import com.example.nalasaka.data.remote.response.ProfileData

// --- Mock Data Tambahan untuk Statistik ---
data class ProfileStats(val following: Int, val products: Int, val followers: Int)

// Fungsi untuk mendapatkan ViewModel Auth
@Composable
fun getAuthViewModel(navController: NavHostController): AuthViewModel {
    return viewModel(factory = ViewModelFactory.getInstance(navController.context))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val profileState by viewModel.profileState.collectAsState()
    val authViewModel = getAuthViewModel(navController)
    val snackbarHostState = remember { SnackbarHostState() }

    // Mock Data Statis
    val mockStats = ProfileStats(following = 5, products = 100, followers = 1500)

    // Daftar Menu Aksi (Contoh: Bisa diubah menjadi navigasi sebenarnya)
    val actionItems = listOf(
        "Ubah Profil",
        "Atur Alamat",
        "Pengaturan Akun",
        "Syarat & Ketentuan",
        "Bantuan"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary), // Burnt Orangeish
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = profileState) {
            is UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Success -> {
                val profile = state.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEBEBEB)) // Background abu-abu muda
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. HEADER PROFIL (DeepMoss Background)
                    ProfileHeader(profile = profile, stats = mockStats)

                    Spacer(modifier = Modifier.height(24.dp))

                    // 2. MENU Aksi
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            actionItems.forEach { label ->
                                ProfileMenuItem(label = label) {
                                    // Aksi klik menu
                                    // snackbarHostState.showSnackbar("Navigasi ke $label")
                                }
                                Divider(thickness = 0.5.dp, color = Color.LightGray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. LOG OUT BUTTON (DeepMoss)
                    PrimaryButton(
                        text = "LOG OUT",
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), // Deep Moss
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. MULAI MENJUAL BUTTON (Link Tekstual - Burnt Orangeish)
                    TextButton(onClick = {
                        /* TODO: Implementasi aktivasi mode seller */
                        // snackbarHostState.showSnackbar("Mengarahkan ke pendaftaran Seller")
                    }) {
                        Text(
                            text = "Mulai Menjual",
                            color = MaterialTheme.colorScheme.primary, // Burnt Orangeish
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = "Gagal memuat profil: ${state.errorMessage}", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun ProfileHeader(profile: ProfileData, stats: ProfileStats) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary) // Deep Moss
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Foto Profil
                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                    modifier = Modifier.size(80.dp)
                ) {
                    AsyncImage(
                        model = profile.photoUrl,
                        contentDescription = profile.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        // --- PERBAIKAN DI SINI: Menggunakan rememberVectorPainter ---
                        error = rememberVectorPainter(Icons.Default.Person),
                        // Jika Anda ingin icon terpusat di tengah dengan ukuran tetap (seperti 50.dp)
                        // Anda juga dapat menambahkan placeholder dan fallback dengan cara yang sama:
                        placeholder = rememberVectorPainter(Icons.Default.Person),
                        fallback = rememberVectorPainter(Icons.Default.Person)
                        // -----------------------------------------------------------
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Nama
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Statistik (Mengikuti | Produk | Pengikut)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(value = stats.following, label = "Mengikuti")
                StatItem(value = stats.products, label = "Produk")
                StatItem(value = stats.followers, label = "Pengikut")
            }
        }
    }
}

@Composable
fun RowScope.StatItem(value: Int, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.weight(1f)
    ) {
        Text(
            text = value.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
fun ProfileMenuItem(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Placeholder Icon (misalnya: menggunakan Person untuk semua)
        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next", tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}