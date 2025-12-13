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
// FIX: Menggunakan AutoMirrored untuk ikon yang mendukung RTL (Right-to-Left)
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
// Import untuk ikon edit (pensil)
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary), // Burnt Orangeish
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Menggunakan AutoMirrored.ArrowBack
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
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
                    ProfileHeader(profile = profile)

                    Spacer(modifier = Modifier.height(24.dp))

                    // NEW: Tampilkan Nama Toko jika user adalah seller
                    if (profile.role == "seller" && profile.storeName != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(
                                    text = "Nama Toko",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = profile.storeName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }


                    // 2. TAMPILAN DETAIL PROFIL AKTUAL
                    ProfileDetailsSection(profile = profile, navController = navController)

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. LOG OUT BUTTON (DeepMoss) - Dipertahankan
                    PrimaryButton(
                        text = "LOG OUT",
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), // Deep Moss
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. MULAI MENJUAL BUTTON (Logic diubah)
                    if (profile.role != "seller") { // Hanya tampilkan jika user BUKAN seller
                        TextButton(onClick = {
                            navController.navigate(Screen.VerifySeller.route) // Navigasi ke verifikasi seller
                        }) {
                            Text(
                                text = "Mulai Menjual",
                                color = MaterialTheme.colorScheme.primary, // Burnt Orangeish
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = "Gagal memuat profil: ${state.errorMessage}", color = MaterialTheme.colorScheme.error) }
        }
    }
}

// ProfileHeader tetap sama
@Composable
fun ProfileHeader(profile: ProfileData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary) // Deep Moss
            .padding(24.dp)
    ) {
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
                    error = rememberVectorPainter(Icons.Default.Person),
                    placeholder = rememberVectorPainter(Icons.Default.Person),
                    fallback = rememberVectorPainter(Icons.Default.Person)
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
    }
}

// Fungsi untuk menampilkan data profil dengan tombol edit tunggal
@Composable
fun ProfileDetailsSection(profile: ProfileData, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) { // Padding untuk konten
                // 1. Nama
                ProfileDetailItem(
                    label = "Nama",
                    value = profile.name
                )
                Divider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                // 2. Email (Tidak bisa diedit)
                ProfileDetailItem(
                    label = "Email",
                    value = profile.email
                )
                Divider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                // 3. Nomor HP
                ProfileDetailItem(
                    label = "Nomor HP",
                    value = profile.phoneNumber ?: "Belum ditambahkan"
                )
                Divider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

                // 4. Alamat
                ProfileDetailItem(
                    label = "Alamat",
                    value = profile.address ?: "Belum ditambahkan"
                )
            }

            // Single Edit Button diposisikan di sudut kanan atas Card
            IconButton(
                onClick = {
                    navController.navigate(Screen.EditProfile.route) // Navigasi ke Edit Profil
                },
                modifier = Modifier
                    .align(Alignment.TopEnd) // Posisikan di sudut kanan atas
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Create, // Ikon pensil/edit
                    contentDescription = "Edit Profil",
                    tint = MaterialTheme.colorScheme.primary // Warna Burnt Orangeish
                )
            }
        }
    }
}

// Komponen Pembantu untuk setiap baris detail
@Composable
fun ProfileDetailItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}