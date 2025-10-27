package com.example.nalasaka.ui.screen.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.viewmodel.DetailViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    sakaId: String,
    navController: NavHostController,
    viewModel: DetailViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val sakaDetailState by viewModel.sakaDetailState.collectAsState()

    LaunchedEffect(sakaId) {
        viewModel.loadSakaDetail(sakaId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Produk", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary), // Burnt Orangeish
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = sakaDetailState) {
            is UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Success -> {
                val saka = state.data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AsyncImage(
                        model = saka.photoUrl,
                        contentDescription = saka.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = saka.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text(text = formatRupiah(saka.price ?: 0), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(text = "Deskripsi:", style = MaterialTheme.typography.titleMedium)
                        Text(text = saka.description, style = MaterialTheme.typography.bodyLarge)

                        Spacer(modifier = Modifier.height(24.dp))

                        // --- FOKUS ANDA: Modul Reputasi & Analisis ---
                        Text(
                            text = "Rating Produk & Ulasan",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Area ini adalah tempat Anda (MHD. HASBI) akan menambahkan komponen Rating, Tombol Tambah Ulasan, dan Daftar Ulasan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(text = "Gagal memuat detail produk: ${state.errorMessage}", color = MaterialTheme.colorScheme.error) }
        }
    }
}