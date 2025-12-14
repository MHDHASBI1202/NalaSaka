package com.example.nalasaka.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
// Perbaikan: Menggunakan ikon AutoMirrored untuk ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable // Import yang diperlukan
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.data.remote.response.ProfileData
import com.example.nalasaka.ui.components.PrimaryButton
// Perbaikan: Mengubah NalaSakaTextField menjadi CustomTextField
import com.example.nalasaka.ui.components.CustomTextField
import com.example.nalasaka.ui.viewmodel.ProfileViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val profileState by viewModel.profileState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    val profile = (profileState as? UiState.Success)?.data ?: ProfileData("0", "", "", null, null, null)

    var name by rememberSaveable { mutableStateOf(profile.name) }
    var phoneNumber by rememberSaveable { mutableStateOf(profile.phoneNumber ?: "") }
    var address by rememberSaveable { mutableStateOf(profile.address ?: "") }
    var storeName by rememberSaveable { mutableStateOf(profile.storeName ?: "") } // NEW: State Nama Toko


    LaunchedEffect(profile) {
        if (profile.name.isNotEmpty() && profileState is UiState.Success) {
            name = profile.name
            phoneNumber = profile.phoneNumber ?: ""
            address = profile.address ?: ""
            storeName = profile.storeName ?: "" // NEW: Sinkronisasi store name
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Profil berhasil diperbarui!")
                navController.popBackStack()
                viewModel.resetUpdateState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar("Gagal: ${state.errorMessage}")
                viewModel.resetUpdateState()
            }
            else -> { /* Do nothing */ }
        }
    }

    val isSaving = updateState is UiState.Loading
    val isSeller = profile.role == "seller" // NEW: Deteksi role

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profil", color = MaterialTheme.colorScheme.onPrimary) },
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
        when (profileState) {
            is UiState.Loading, UiState.Idle -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = "Gagal memuat profil: ${(profileState as UiState.Error).errorMessage}", color = MaterialTheme.colorScheme.error) }
            is UiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Edit informasi profil Anda. Email hanya bisa diganti melalui rute terpisah.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // NEW: Field Nama Toko jika user adalah seller
                    if (isSeller) {
                        Text(
                            text = "Informasi Penjual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        CustomTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = "Nama Toko",
                            placeholder = "Masukkan nama toko Anda",
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 16.dp),
                            thickness = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Informasi Akun",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Input Nama
                    CustomTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nama Lengkap",
                        placeholder = "Masukkan nama Anda",
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )

                    // Input Email (Tidak dapat diubah)
                    OutlinedTextField(
                        value = profile.email,
                        onValueChange = { },
                        label = { Text("Email (Tidak Dapat Diubah)") },
                        readOnly = true,
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledBorderColor = Color.LightGray
                        )
                    )

                    // Input Nomor HP
                    CustomTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "Nomor HP",
                        placeholder = "Masukkan nomor handphone",
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    )

                    // Input Alamat
                    CustomTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "Alamat",
                        placeholder = "Masukkan alamat Anda",
                        maxLines = 4, // Multi-line
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                    )


                    // Tombol Simpan
                    PrimaryButton(
                        text = if (isSaving) "Menyimpan..." else "SIMPAN PERUBAHAN",
                        onClick = {
                            // Kirim storeName jika user adalah seller
                            viewModel.updateProfile(name, phoneNumber, address, if (isSeller) storeName else null)
                        },
                        enabled = !isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}