package com.example.nalasaka.ui.screen.produk

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.ui.components.ProductGridItem
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.HomeViewModel // Reusing HomeViewModel for data fetch
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@Composable
fun ProductScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val sakaState by viewModel.sakaState.collectAsState()

    // Asumsi: Kita akan memfilter data berdasarkan state lokal
    var selectedMainFilter by remember { mutableStateOf("Sayur") } // Buah/Sayur
    var selectedSubFilter by remember { mutableStateOf("Semua") } // Semua, Terlaris, Hijau, dll.
    var searchText by remember { mutableStateOf("") }

    val userModel by viewModel.repository.getUser().collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false)
    )

    LaunchedEffect(userModel.isLogin) {
        if (userModel.isLogin && (sakaState is UiState.Idle || sakaState is UiState.Error)) {
            viewModel.loadSaka(userModel.token)
        }
    }

    // Mock Data Filter
    val mainFilters = listOf("Buah", "Sayur")
    val subFilters = listOf("Semua", "Terlaris", "Sayuran Hijau", "Import", "Lokal")

    // Filter Data List
    val filteredList = remember(sakaState, selectedMainFilter, selectedSubFilter) {
        val list = (sakaState as? UiState.Success<List<SakaItem>>)?.data ?: emptyList()
        list.filter { item ->
            // Implementasi filtering mock sederhana berdasarkan nama
            val isMainMatch = when (selectedMainFilter) {
                "Sayur" -> item.name.contains("Bawang", ignoreCase = true) || item.name.contains("Lettuce", ignoreCase = true) || item.name.contains("Pakcoy", ignoreCase = true)
                "Buah" -> item.name.contains("Tomat", ignoreCase = true) || item.name.contains("Nanas", ignoreCase = true) // Tomat dikategorikan buah
                else -> true
            }
            isMainMatch && (selectedSubFilter == "Semua" || item.name.contains(selectedSubFilter, ignoreCase = true))
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F0E6))) { // Light Background seperti di gambar
        // 1. TOP BAR
        ProductTopBar(navController = navController, searchText = searchText, onSearchTextChange = { searchText = it })

        // 2. MAIN CATEGORY FILTER (Buah/Sayur)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            mainFilters.forEach { filter ->
                CategoryChip(
                    label = filter,
                    isSelected = filter == selectedMainFilter,
                    onClick = { selectedMainFilter = it; selectedSubFilter = "Semua" }, // Reset sub-filter saat kategori utama diganti
                    isMain = true
                )
            }
        }

        // 3. SUB-CATEGORY FILTER (Semua, Terlaris, dll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            subFilters.forEach { filter ->
                CategoryChip(
                    label = filter,
                    isSelected = filter == selectedSubFilter,
                    onClick = { selectedSubFilter = it },
                    isMain = false
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. GRID PRODUK
        when (sakaState) {
            UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) { Text("Gagal memuat produk: ${ (sakaState as UiState.Error).errorMessage }", color = MaterialTheme.colorScheme.error) }
            is UiState.Success -> {
                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tidak ada produk ditemukan.") }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredList, key = { it.id }) { saka ->
                            ProductGridItem(saka = saka, onClick = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) })
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) } // Untuk mengimbangi bottom bar
                    }
                }
            }
        }
    }
}

// Komponen Top Bar untuk Halaman Produk
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductTopBar(
    navController: NavHostController,
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = { Text("Cari Kebutuhanmu") },
                leadingIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Back button
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        },
        actions = {
            IconButton(onClick = { /* Navigasi ke Keranjang */ }) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
    // Divider di bawah Top Bar (opsional, tergantung desain)
    // Divider(color = Color.LightGray, thickness = 0.5.dp)
}


// Komponen Chip untuk Filter
@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: (String) -> Unit,
    isMain: Boolean = false // Main filter (Buah/Sayur) vs Sub filter (Semua, Terlaris)
) {
    val containerColor = when {
        isSelected && isMain -> MaterialTheme.colorScheme.primary // Burnt Orangeish
        !isSelected && isMain -> Color.White
        isSelected && !isMain -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f) // Deep Moss
        else -> Color(0xFFEBEBEB) // Greyish-tan
    }

    val contentColor = when {
        isSelected && isMain -> Color.White
        isSelected && !isMain -> MaterialTheme.colorScheme.secondary
        !isSelected && isMain -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }

    // Placeholder Icon untuk filter utama (Buah/Sayur)
    val icon = when (label) {
        "Buah" -> Icons.Default.Fastfood
        "Sayur" -> Icons.Default.LocalFlorist
        else -> null
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (isMain) 4.dp else 0.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick(label) }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = label, tint = contentColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                color = contentColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}