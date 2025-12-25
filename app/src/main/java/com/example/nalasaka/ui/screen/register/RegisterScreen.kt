package com.example.nalasaka.ui.screen.register

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
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val registerState by viewModel.registerState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneNumberError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }


    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is UiState.Success -> {
                dialogTitle = "Pendaftaran Berhasil!"
                dialogMessage = "Akun Anda telah berhasil dibuat. Silakan masuk untuk memulai."
                showDialog = true
            }
            is UiState.Error -> {
                dialogTitle = "Gagal Mendaftar"
                dialogMessage = state.errorMessage
                showDialog = true
            }
            else -> {}
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                if (registerState is UiState.Success) {
                    navController.navigate(Screen.Login.route) { popUpTo(Screen.Register.route) { inclusive = true } }
                }
            },
            title = {
                Text(
                    dialogTitle,
                    color = if (registerState is UiState.Success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            },
            text = { Text(dialogMessage) },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        if (registerState is UiState.Success) {
                            navController.navigate(Screen.Login.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Pengguna Baru", color = MaterialTheme.colorScheme.onPrimary) },
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
            Text(text = "Buat Akun NalaSaka", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            CustomTextField(value = name, onValueChange = { name = it; nameError = null }, label = "Nama Lengkap", isError = nameError != null, errorMessage = nameError)

            CustomTextField(value = email, onValueChange = { email = it; emailError = null }, label = "Email", keyboardType = KeyboardType.Email, isError = emailError != null, errorMessage = emailError)

            CustomTextField(value = phoneNumber, onValueChange = { phoneNumber = it; phoneNumberError = null }, label = "Nomor Handphone", keyboardType = KeyboardType.Phone, isError = phoneNumberError != null, errorMessage = phoneNumberError)
            CustomTextField(value = address, onValueChange = { address = it; addressError = null }, label = "Alamat Lengkap", keyboardType = KeyboardType.Text, isError = addressError != null, errorMessage = addressError)

            CustomTextField(value = password, onValueChange = { password = it; passwordError = null; confirmPasswordError = null }, label = "Password (min 8 karakter)", keyboardType = KeyboardType.Password, isError = passwordError != null, errorMessage = passwordError)

            CustomTextField(value = confirmPassword, onValueChange = { confirmPassword = it; confirmPasswordError = null }, label = "Konfirmasi Password", keyboardType = KeyboardType.Password, isError = confirmPasswordError != null, errorMessage = confirmPasswordError)

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "DAFTAR",
                onClick = {
                    if (validateRegisterInput(
                            name,
                            email,
                            phoneNumber,
                            address,
                            password,
                            confirmPassword,
                            { nameError = it },
                            { emailError = it },
                            { phoneNumberError = it },
                            { addressError = it },
                            { passwordError = it },
                            { confirmPasswordError = it }
                        )
                    ) {
                        viewModel.register(name, email, password, phoneNumber, address, confirmPassword)
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

private fun validateRegisterInput(
    name: String,
    email: String,
    phoneNumber: String,
    address: String,
    password: String,
    confirmPassword: String,
    onNameError: (String?) -> Unit,
    onEmailError: (String?) -> Unit,
    onPhoneNumberError: (String?) -> Unit,
    onAddressError: (String?) -> Unit,
    onPasswordError: (String?) -> Unit,
    onConfirmPasswordError: (String?) -> Unit
): Boolean {
    var isValid = true

    onNameError(null)
    onEmailError(null)
    onPhoneNumberError(null)
    onAddressError(null)
    onPasswordError(null)
    onConfirmPasswordError(null)

    if (name.isBlank()) {
        onNameError("Nama wajib diisi"); isValid = false
    }

    if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        onEmailError("Email tidak valid"); isValid = false
    }

    if (phoneNumber.isBlank()) {
        onPhoneNumberError("Nomor HP wajib diisi"); isValid = false
    } else if (phoneNumber.length < 10) {
        onPhoneNumberError("Nomor HP minimal 10 digit"); isValid = false
    }

    if (address.isBlank()) {
        onAddressError("Alamat wajib diisi"); isValid = false
    }

    if (password.isBlank() || password.length < 8) {
        onPasswordError("Password minimal 8 karakter"); isValid = false
    }

    if (confirmPassword.isBlank() || confirmPassword != password) {
        onConfirmPasswordError("Konfirmasi password tidak cocok"); isValid = false
    }

    return isValid
}