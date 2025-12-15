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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.ui.components.ProductGridItem
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.HomeViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@Composable
fun ProductScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val sakaState by viewModel.sakaState.collectAsState()

    // 1. [PERBAIKAN] Default filter adalah "Semua" agar semua data tampil di awal
    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    // State untuk Search
    var searchText by remember { mutableStateOf("") }

    val userModel by viewModel.repository.getUser().collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false)
    )

    // Load data jika belum ada
    LaunchedEffect(userModel.isLogin) {
        if (userModel.isLogin) {
            viewModel.loadSaka(userModel.token)
        }
    }

    // Daftar Kategori (Sesuai dengan yang ada di AddSakaScreen)
    val categories = listOf("Semua", "Sayur", "Buah", "Rempah", "Beras/Biji-bijian", "Lainnya")

    // 2. [PERBAIKAN] Logika Filter menggunakan data 'category' dari Database
    val filteredList = remember(sakaState, selectedCategoryFilter, searchText) {
        val list = (sakaState as? UiState.Success<List<SakaItem>>)?.data ?: emptyList()

        list.filter { item ->
            // Filter Kategori
            val isCategoryMatch = if (selectedCategoryFilter == "Semua") {
                true // Tampilkan semua
            } else {
                // Cocokkan string kategori dari database (ignore case)
                item.category.equals(selectedCategoryFilter, ignoreCase = true)
            }

            // Filter Pencarian (Nama Produk)
            val isSearchMatch = if (searchText.isBlank()) {
                true
            } else {
                item.name.contains(searchText, ignoreCase = true)
            }

            isCategoryMatch && isSearchMatch
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F0E6))) {
        // 1. TOP BAR
        ProductTopBar(
            navController = navController,
            searchText = searchText,
            onSearchTextChange = { searchText = it }
        )

        // 2. KATEGORI FILTER (Horizontal Scroll)
        // Menampilkan chip kategori dari database
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onSurfaceVariant)

            categories.forEach { category ->
                CategoryChip(
                    label = category,
                    isSelected = category == selectedCategoryFilter,
                    onClick = { selectedCategoryFilter = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. GRID PRODUK
        when (sakaState) {
            UiState.Idle, UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Gagal memuat produk: ${(sakaState as UiState.Error).errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is UiState.Success -> {
                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tidak ada produk ditemukan.", fontWeight = FontWeight.Bold)
                            if (selectedCategoryFilter != "Semua") {
                                Text("Coba ganti kategori lain.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredList, key = { it.id }) { saka ->
                            ProductGridItem(
                                saka = saka,
                                onClick = { sakaId ->
                                    navController.navigate(Screen.Detail.createRoute(sakaId))
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) } // Padding bawah untuk BottomBar
                    }
                }
            }
        }
    }
}

// Komponen Top Bar
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
                placeholder = { Text("Cari produk...") },
                leadingIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        // Opsi clear text bisa ditambahkan di sini
                    } else {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
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
}


// Komponen Chip untuk Filter
@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: (String) -> Unit
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        modifier = Modifier
            .height(40.dp) // Tinggi fix agar rapi
            .clickable { onClick(label) }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Icon kecil opsional jika kategori tertentu
            if (label == "Semua") {
                Icon(Icons.Default.Category, null, tint = contentColor, modifier = Modifier.size(16.dp))
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