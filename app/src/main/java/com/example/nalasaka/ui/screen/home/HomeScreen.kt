package com.example.nalasaka.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.SakaItem
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.HomeViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context)),
    viewModel: HomeViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val sakaState by viewModel.sakaState.collectAsState()

    // --- PERBAIKAN: Menggunakan userSession dari AuthViewModel ---
    val userModel by authViewModel.userSession.collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false)
    )
    // ----------------------------------------------------------------

    LaunchedEffect(userModel.isLogin) {
        if (userModel.isLogin) {
            viewModel.loadSaka(userModel.token)
        } else {
            navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NalaSaka - Produk Tani", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary), // Burnt Orangeish
                actions = {
                    IconButton(onClick = { viewModel.loadSaka(userModel.token) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Produk", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddSaka.route) },
                containerColor = MaterialTheme.colorScheme.secondary // Deep Moss
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Produk (Upload Foto Barang)", tint = MaterialTheme.colorScheme.onSecondary)
            }
        }
    ) { paddingValues ->
        when (val state = sakaState) {
            UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Success -> {
                val listSaka = state.data
                if (listSaka.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Belum ada produk. Ayo tambah produk pertama Anda!") }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(listSaka, key = { it.id }) { saka ->
                            SakaItem(saka = saka, onClick = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) })
                        }
                    }
                }
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = "ERROR: ${state.errorMessage}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
        }
    }
}