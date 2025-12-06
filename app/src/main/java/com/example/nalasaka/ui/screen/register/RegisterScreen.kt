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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction // Tambahkan ini jika diperlukan
import androidx.compose.foundation.text.KeyboardOptions // Penting: Tambahkan impor ini
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.MyTextField // DIGANTI: Menggunakan MyTextField yang benar
import com.example.nalasaka.ui.components.PasswordTextField
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavHostController,
    // Menggunakan LocalContext untuk inisialisasi Repository
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val registerState by viewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- State Input ---
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isSeller by remember { mutableStateOf(false) }

    // --- State Error ---
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }


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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary),
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

            // --- INPUT FIELDS ---
            MyTextField(
                value = name,
                onValueChange = { newValue -> name = newValue; nameError = null },
                label = "Nama Lengkap",
                isError = nameError != null,
                errorMessage = nameError
            )
            MyTextField(
                value = phoneNumber,
                onValueChange = { newValue -> phoneNumber = newValue; phoneNumberError = null },
                label = "Nomor HP",
                // PERBAIKAN: Mengganti keyboardType dengan keyboardOptions
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = phoneNumberError != null,
                errorMessage = phoneNumberError
            )
            MyTextField(
                value = address,
                onValueChange = { newValue -> address = newValue; addressError = null },
                label = "Alamat",
                isError = addressError != null,
                errorMessage = addressError
            )
            MyTextField(
                value = email,
                onValueChange = { newValue -> email = newValue; emailError = null },
                label = "Email/ID",
                // PERBAIKAN: Mengganti keyboardType dengan keyboardOptions
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailError != null,
                errorMessage = emailError
            )

            // Password Fields (menggunakan PasswordTextField untuk tombol mata)
            PasswordTextField(
                value = password,
                onValueChange = { newValue -> password = newValue; passwordError = null; confirmPasswordError = null },
                label = "Password (min 8 karakter)",
                isError = passwordError != null,
                errorMessage = passwordError
            )
            PasswordTextField(
                value = confirmPassword,
                onValueChange = { newValue -> confirmPassword = newValue; confirmPasswordError = null },
                label = "Konfirmasi Password",
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError
            )

            // Checkbox "Saya ingin langsung menjadi Penjual"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isSeller,
                    onCheckedChange = { isSeller = it }
                )
                Text(
                    text = "Saya ingin langsung menjadi Penjual",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "DAFTAR",
                onClick = {
                    if (validateAllRegisterInput(
                            name, email, password, phoneNumber, address, confirmPassword,
                            { nameError = it }, { emailError = it }, { passwordError = it },
                            { phoneNumberError = it }, { addressError = it }, { confirmPasswordError = it }
                        )) {
                        // Panggil fungsi register dengan semua 6 parameter
                        viewModel.register(name, email, password, phoneNumber, address, isSeller)
                    }
                },
                isLoading = registerState is UiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sudah punya akun? ")
                TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                    Text("Login di sini", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

// Fungsi validasi dipertahankan dan sudah benar.
private fun validateAllRegisterInput(
    name: String,
    email: String,
    password: String,
    phoneNumber: String,
    address: String,
    confirmPassword: String,
    onNameError: (String?) -> Unit,
    onEmailError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit,
    onPhoneNumberError: (String?) -> Unit,
    onAddressError: (String?) -> Unit,
    onConfirmPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    // Reset semua error
    onNameError(null)
    onEmailError(null)
    onPasswordError(null)
    onPhoneNumberError(null)
    onAddressError(null)
    onConfirmPasswordError(null)

    if (name.isBlank()) { onNameError("Nama wajib diisi"); isValid = false }
    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) { onEmailError("Email tidak valid"); isValid = false }
    if (phoneNumber.isBlank()) { onPhoneNumberError("Nomor HP wajib diisi"); isValid = false }
    if (address.isBlank()) { onAddressError("Alamat wajib diisi"); isValid = false }

    if (password.isBlank() || password.length < 8) {
        onPasswordError("Password minimal 8 karakter");
        isValid = false
    } else if (password != confirmPassword) {
        onConfirmPasswordError("Konfirmasi password tidak cocok");
        isValid = false
    }

    if (confirmPassword.isBlank()) {
        onConfirmPasswordError("Konfirmasi password wajib diisi");
        isValid = false
    } else if (password != confirmPassword) {
        onConfirmPasswordError("Konfirmasi password tidak cocok");
        isValid = false
    }

    return isValid
}