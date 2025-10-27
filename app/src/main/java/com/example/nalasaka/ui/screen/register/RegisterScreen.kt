package com.example.nalasaka.ui.screen.register

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.CustomTextField
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val registerState by viewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(message = "Pendaftaran berhasil! Silakan Login.", duration = SnackbarDuration.Long)
                navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } }
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
                title = { Text("Daftar Pengguna Baru", color = MaterialTheme.colorScheme.onPrimary) },
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
            Text(text = "Buat Akun NalaSaka", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            CustomTextField(value = name, onValueChange = { name = it; nameError = null }, label = "Nama Lengkap", isError = nameError != null, errorMessage = nameError)
            CustomTextField(value = email, onValueChange = { email = it; emailError = null }, label = "Email", keyboardType = KeyboardType.Email, isError = emailError != null, errorMessage = emailError)
            CustomTextField(value = password, onValueChange = { password = it; passwordError = null }, label = "Password (min 8 karakter)", keyboardType = KeyboardType.Password, isError = passwordError != null, errorMessage = passwordError)

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "DAFTAR",
                onClick = {
                    if (validateRegisterInput(name, email, password, { nameError = it }, { emailError = it }, { passwordError = it })) {
                        viewModel.register(name, email, password)
                    }
                },
                isLoading = registerState is UiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sudah punya akun? ")
                TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                    Text("Login di sini", color = MaterialTheme.colorScheme.secondary) // Deep Moss
                }
            }
        }
    }
}

// Fungsi validasi ditempatkan di luar Composable
private fun validateRegisterInput(name: String, email: String, password: String, onNameError: (String) -> Unit, onEmailError: (String) -> Unit, onPasswordError: (String) -> Unit): Boolean {
    var isValid = true
    if (name.isBlank()) { onNameError("Nama wajib diisi"); isValid = false }
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { onEmailError("Email tidak valid"); isValid = false }
    if (password.isBlank() || password.length < 8) { onPasswordError("Password minimal 8 karakter"); isValid = false }
    return isValid
}