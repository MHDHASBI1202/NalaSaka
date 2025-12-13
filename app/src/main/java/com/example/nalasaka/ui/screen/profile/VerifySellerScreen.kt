package com.example.nalasaka.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.components.CustomTextField
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.ProfileViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifySellerScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val profileState by viewModel.profileState.collectAsState()
    val activationState by viewModel.sellerActivationState.collectAsState()

    val profile = (profileState as? UiState.Success)?.data

    // State lokal untuk Nama Toko dan persetujuan
    var storeName by rememberSaveable { mutableStateOf("") }
    var isConfirmed by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Efek samping untuk menangani hasil aktivasi
    LaunchedEffect(activationState) {
        when (val state = activationState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Selamat! Anda kini adalah penjual: ${state.data.storeName}")
                // Kembali ke ProfileScreen dan memicu refresh
                navController.navigate(Screen.Profile.route) {
                    popUpTo(Screen.Profile.route) { inclusive = true }
                }
                viewModel.resetSellerActivationState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar("Gagal aktivasi penjual: ${state.errorMessage}")
                viewModel.resetSellerActivationState()
            }
            else -> { /* Do nothing */ }
        }
    }

    val isActivating = activationState is UiState.Loading
    val isProfileLoaded = profileState is UiState.Success

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verifikasi Penjual", color = MaterialTheme.colorScheme.onPrimary) },
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
        when {
            !isProfileLoaded || profile == null -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                if (profileState is UiState.Error) {
                    Text("Gagal memuat data profil.", color = MaterialTheme.colorScheme.error)
                } else {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "Langkah terakhir untuk mulai menjual. Pastikan data di bawah sudah benar.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // DETAIL DATA USER UNTUK VERIFIKASI
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            VerificationDetailItem("Nama", profile.name)
                            Divider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                            VerificationDetailItem("Email", profile.email)
                            Divider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                            VerificationDetailItem("Nomor HP", profile.phoneNumber ?: "-")
                            Divider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                            VerificationDetailItem("Alamat", profile.address ?: "-")
                        }
                    }

                    // OPSI EDIT
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ada yang ingin diubah?",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { navController.navigate(Screen.EditProfile.route) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Create, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Profil")
                            }
                        }
                    }

                    Divider(thickness = 1.dp, color = Color.LightGray.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 24.dp))

                    // INPUT NAMA TOKO
                    CustomTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = "Nama Toko",
                        placeholder = "Masukkan nama toko Anda",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                    )

                    // CHECKBOX KONFIRMASI
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isConfirmed,
                            onCheckedChange = { isConfirmed = it },
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Saya setuju untuk mengaktifkan mode penjual dan Nama Toko sudah benar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // TOMBOL AKTIVASI
                    PrimaryButton(
                        text = if (isActivating) "Mengaktifkan..." else "AKTIFKAN MODE PENJUAL",
                        onClick = {
                            viewModel.activateSellerMode(storeName)
                        },
                        enabled = isConfirmed && storeName.isNotBlank() && !isActivating,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}