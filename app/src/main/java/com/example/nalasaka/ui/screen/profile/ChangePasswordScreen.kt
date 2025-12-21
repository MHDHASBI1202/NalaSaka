package com.example.nalasaka.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.CustomTextField
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.viewmodel.ProfileViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val changePassState by viewModel.changePassState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(changePassState) {
        if (changePassState is UiState.Success) {
            snackbarHostState.showSnackbar("Password berhasil diubah!")
            viewModel.resetChangePassState()
            navController.popBackStack()
        } else if (changePassState is UiState.Error) {
            snackbarHostState.showSnackbar((changePassState as UiState.Error).errorMessage)
            viewModel.resetChangePassState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ganti Password") },
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
                .verticalScroll(rememberScrollState())
        ) {
            CustomTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = "Password Saat Ini",
                keyboardType = KeyboardType.Password
            )

            CustomTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Password Baru",
                keyboardType = KeyboardType.Password
            )

            CustomTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Konfirmasi Password Baru",
                keyboardType = KeyboardType.Password
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "SIMPAN PASSWORD",
                onClick = {
                    viewModel.changePassword(currentPassword, newPassword, confirmPassword)
                },
                isLoading = changePassState is UiState.Loading,
                enabled = currentPassword.isNotEmpty() && newPassword.isNotEmpty() && confirmPassword.isNotEmpty()
            )
        }
    }
}