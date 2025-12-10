package com.example.nalasaka.ui.screen.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val loginState by viewModel.loginState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // State untuk AlertDialog
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is UiState.Success -> {
                // Navigasi ke Home
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Welcome.route) { inclusive = true }
                }
            }
            is UiState.Error -> {
                // Tampilkan pop-up Error
                dialogTitle = "Gagal Login"
                // Pesan error sudah diurai di Repository
                dialogMessage = state.errorMessage
                showDialog = true
            }
            else -> {}
        }
    }

    // AlertDialog Composable
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    dialogTitle,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column {
                    Text(dialogMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                    // Tambahkan CTA Register jika error terkait kredensial salah (karena akun bisa jadi belum terdaftar)
                    if (dialogMessage.contains("Email atau Password yang Anda masukkan salah")) {
                        TextButton(onClick = {
                            showDialog = false
                            navController.navigate(Screen.Register.route)
                        }) {
                            Text("Daftar Akun Baru", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Tutup")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login Pengguna", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
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

            CustomTextField(value = email, onValueChange = { email = it; emailError = null }, label = "Email", keyboardType = KeyboardType.Email, isError = emailError != null, errorMessage = emailError)
            CustomTextField(value = password, onValueChange = { password = it; passwordError = null }, label = "Password (min 8 karakter)", keyboardType = KeyboardType.Password, isError = passwordError != null, errorMessage = passwordError)

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "LOGIN",
                onClick = { if (validateAuthInput(email, password, { emailError = it }, { passwordError = it })) { viewModel.login(email, password) } },
                isLoading = loginState is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Belum punya akun? ")
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text("Daftar di sini", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

// Fungsi validasi ditempatkan di luar Composable
private fun validateAuthInput(email: String, password: String, onEmailError: (String) -> Unit, onPasswordError: (String) -> Unit): Boolean {
    var isValid = true
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { onEmailError("Email tidak valid"); isValid = false }
    if (password.isBlank() || password.length < 8) { onPasswordError("Password minimal 8 karakter"); isValid = false }
    return isValid
}