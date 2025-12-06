package com.example.nalasaka.ui.screen.seller

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Import baru
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nalasaka.di.Injection
import com.example.nalasaka.ui.components.MyTextField
import com.example.nalasaka.ui.viewmodel.SellerViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerVerificationScreen(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // 1. Dapatkan Context lokal
    val context = LocalContext.current

    // 2. Inisialisasi Repository menggunakan Context (membutuhkan modifikasi pada Injection.kt)
    val repository = Injection.provideRepository(context)

    // 3. Inisialisasi ViewModel menggunakan Factory dan Repository
    val viewModel: SellerViewModel = viewModel(
        factory = ViewModelFactory(repository)
    )

    // 4. Akses data pengguna dari Repository yang sudah diinisialisasi
    val userPref = repository.getUser()
    val userModel by userPref.collectAsState(initial = null)

    LaunchedEffect(userModel) {
        // ... (Logika inisialisasi data dipertahankan)
    }

    // LaunchedEffect untuk menangani navigasi setelah sukses
    LaunchedEffect(viewModel.uiState) {
        when (val state = viewModel.uiState) {
            is UiState.Success<*> -> {
                // Tampilkan Snackbar/Toast untuk pesan sukses
                navigateToHome()
            }
            is UiState.Error -> {
                // Tampilkan Snackbar/Toast untuk pesan error
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verifikasi Data Penjual") },
                navigationIcon = {
                    IconButton(onClick = navigateBack) {
                        // Icon back
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Mohon lengkapi atau perbarui data Anda untuk menjadi Penjual.",
                style = MaterialTheme.typography.bodyMedium
            )

            MyTextField(
                value = viewModel.name,
                onValueChange = viewModel::updateName,
                label = "Nama Lengkap"
            )
            MyTextField(
                value = viewModel.phoneNumber,
                onValueChange = viewModel::updatePhoneNumber,
                label = "Nomor HP"
            )
            MyTextField(
                value = viewModel.address,
                onValueChange = viewModel::updateAddress,
                label = "Alamat (Harus diisi)"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = viewModel::becomeSeller,
                enabled = viewModel.uiState !is UiState.Loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (viewModel.uiState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Verifikasi dan Mulai Menjual")
                }
            }
        }
    }
}