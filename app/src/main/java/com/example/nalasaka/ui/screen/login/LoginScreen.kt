package com.example.nalasaka.ui.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions // Import KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.MyTextField // DIGANTI: MyTextField untuk Email
import com.example.nalasaka.ui.components.PasswordTextField // PasswordTextField sudah ada di impor
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
) {
    // FIX: Gunakan LocalContext untuk inisialisasi ViewModel
    val context = LocalContext.current
    val viewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(context))

    val loginState by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is UiState.Success -> {
                // Navigasi ke Home
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(message = state.errorMessage, duration = SnackbarDuration.Long)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login Pengguna", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary), // Deep Moss
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Masuk ke Akun Anda", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            // FIX: Menggunakan MyTextField dan KeyboardOptions
            MyTextField(
                value = email,
                onValueChange = { email = it; emailError = null }, // 'it' aman di sini
                label = "Email",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                errorMessage = emailError
            )

            // FIX: Menggunakan PasswordTextField
            PasswordTextField(
                value = password,
                onValueChange = { password = it; passwordError = null }, // 'it' aman di sini
                label = "Password (min 8 karakter)",
                isError = passwordError != null,
                errorMessage = passwordError
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "LOGIN",
                onClick = { if (validateAuthInput(email, password, { emailError = it ?: "" }, { passwordError = it ?: "" })) { viewModel.login(email, password) } },
                isLoading = loginState is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) // Deep Moss
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Belum punya akun? ")
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text("Daftar di sini", color = MaterialTheme.colorScheme.secondary) // Deep Moss
                }
            }
        }
    }
}

// Fungsi validasi dipertahankan
private fun validateAuthInput(email: String, password: String, onEmailError: (String?) -> Unit, onPasswordError: (String?) -> Unit): Boolean {
    var isValid = true
    onEmailError(null)
    onPasswordError(null)

    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError("Email tidak valid");
        isValid = false
    }
    if (password.isBlank() || password.length < 8) {
        onPasswordError("Password minimal 8 karakter");
        isValid = false
    }
    return isValid
}