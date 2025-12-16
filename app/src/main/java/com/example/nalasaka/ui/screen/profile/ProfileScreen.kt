package com.example.nalasaka.ui.screen.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.VerifiedUser
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
import com.example.nalasaka.ui.theme.BurntOrangeish

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

    // [FIX] Force reload profil setiap kali layar ini dibuka/aktif
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                        .background(Color(0xFFEBEBEB))
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(profile = profile)

                    Spacer(modifier = Modifier.height(24.dp))

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
                                // [PERBAIKAN] Tampilkan Nama Toko + Centang jika verified
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.storeName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (profile.verificationStatus == "verified") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Verified Store",
                                            tint = Color(0xFF07C91F),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    ProfileDetailsSection(profile = profile, navController = navController)

                    Spacer(modifier = Modifier.height(32.dp))

                    PrimaryButton(
                        text = "LOG OUT",
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // LOGIKA TOMBOL STATUS & VERIFIKASI
                    if (profile.role != "seller") {
                        // Kalau bukan seller -> Tawarkan jadi seller
                        TextButton(onClick = {
                            navController.navigate(Screen.VerifySeller.route)
                        }) {
                            Text(
                                text = "Mulai Menjual",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        // Kalau SUDAH seller, cek status verifikasi
                        when (profile.verificationStatus) {
                            "verified" -> {
                                // Sudah Verified -> Tampilkan Badge Status Teks
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF07C91F))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Akun Penjual Terverifikasi", color = Color(0xFF07C91F), fontWeight = FontWeight.Bold)
                                }
                            }
                            "pending" -> {
                                Text("Menunggu Verifikasi...", color = Color.Gray)
                            }
                            else -> { // 'none' atau 'rejected'
                                // Belum Verified -> Tawarkan Upload Dokumen
                                TextButton(onClick = {
                                    navController.navigate(Screen.UploadCertification.route)
                                }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.VerifiedUser, null, tint = BurntOrangeish)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Verifikasi Akun (Upload Dokumen)",
                                            color = BurntOrangeish,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = "Gagal memuat profil: ${state.errorMessage}", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun ProfileHeader(profile: ProfileData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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

            // [PERBAIKAN] Tampilkan Nama + Centang di Header Profil
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (profile.verificationStatus == "verified") {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Verified User",
                        tint = Color(0xFF07C91F),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

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
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                ProfileDetailItem(label = "Nama", value = profile.name)
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                ProfileDetailItem(label = "Email", value = profile.email)
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                ProfileDetailItem(label = "Nomor HP", value = profile.phoneNumber ?: "Belum ditambahkan")
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                ProfileDetailItem(label = "Alamat", value = profile.address ?: "Belum ditambahkan")
            }
            IconButton(
                onClick = { navController.navigate(Screen.EditProfile.route) },
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Edit Profil",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}