package com.example.nalasaka.ui.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.CustomTextField
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val forgotState by viewModel.forgotState.collectAsState()
    val resetState by viewModel.resetPassState.collectAsState()

    // Step 1: Input Email -> Step 2: Input Token & New Pass
    var currentStep by remember { mutableIntStateOf(1) }

    var email by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // State untuk Dialog
    var showTokenSentDialog by remember { mutableStateOf(false) }
    var showResetSuccessDialog by remember { mutableStateOf(false) }

    // Logic saat Request Token (Langkah 1)
    LaunchedEffect(forgotState) {
        if (forgotState is UiState.Success) {
            // Tampilkan Dialog Token Terkirim
            showTokenSentDialog = true
            viewModel.resetState()
        } else if (forgotState is UiState.Error) {
            snackbarHostState.showSnackbar((forgotState as UiState.Error).errorMessage)
            viewModel.resetState()
        }
    }

    // Logic saat Reset Password (Langkah 2)
    LaunchedEffect(resetState) {
        if (resetState is UiState.Success) {
            // Tampilkan Dialog Sukses Reset
            showResetSuccessDialog = true
            viewModel.resetState()
        } else if (resetState is UiState.Error) {
            snackbarHostState.showSnackbar((resetState as UiState.Error).errorMessage)
            viewModel.resetState()
        }
    }

    // --- RENDER DIALOGS ---

    if (showTokenSentDialog) {
        CustomSuccessDialog(
            title = "Token Terkirim!",
            message = "Kami telah mengirimkan token reset password ke email/log Anda. Silakan cek dan masukkan token tersebut.",
            icon = Icons.Default.MarkEmailRead,
            buttonText = "Masukkan Token",
            onDismiss = { showTokenSentDialog = false },
            onConfirm = {
                showTokenSentDialog = false
                currentStep = 2 // Pindah ke langkah input token
            }
        )
    }

    if (showResetSuccessDialog) {
        CustomSuccessDialog(
            title = "Password Direset!",
            message = "Password Anda berhasil diperbarui. Silakan login kembali menggunakan password baru.",
            icon = Icons.Default.CheckCircle,
            buttonText = "Ke Halaman Login",
            onDismiss = { /* Tidak bisa dismiss sembarangan, harus klik tombol */ },
            onConfirm = {
                showResetSuccessDialog = false
                navController.popBackStack() // Kembali ke Login
            }
        )
    }

    // --- MAIN UI ---

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lupa Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (currentStep == 1) {
                Text(
                    "Masukkan email Anda. Kami akan mengirimkan token untuk mereset password.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email Terdaftar",
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "KIRIM TOKEN",
                    onClick = { viewModel.requestForgotPassword(email) },
                    isLoading = forgotState is UiState.Loading,
                    enabled = email.isNotEmpty()
                )
            } else {
                Text(
                    "Masukkan token yang dikirim ke email $email dan buat password baru.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(24.dp))

                CustomTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = "Token Reset",
                    placeholder = "Contoh: aX8s..."
                )

                CustomTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Password Baru",
                    keyboardType = KeyboardType.Password
                )

                CustomTextField(
                    value = confirmNewPassword,
                    onValueChange = { confirmNewPassword = it },
                    label = "Konfirmasi Password",
                    keyboardType = KeyboardType.Password
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryButton(
                    text = "RESET PASSWORD",
                    onClick = { viewModel.executeResetPassword(email, token, newPassword, confirmNewPassword) },
                    isLoading = resetState is UiState.Loading,
                    enabled = token.isNotEmpty() && newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty()
                )
            }
        }
    }
}

// --- REUSABLE COMPONENT: CUSTOM SUCCESS DIALOG ---
@Composable
fun CustomSuccessDialog(
    title: String,
    message: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    buttonText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Ikon Besar
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50), // Hijau Sukses (atau bisa pakai MaterialTheme.colorScheme.primary)
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}