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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
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

    var selectedCategoryFilter by remember { mutableStateOf("Semua") }

    var selectedSortOption by remember { mutableStateOf("Default") }
    var showSortMenu by remember { mutableStateOf(false) }

    var searchText by remember { mutableStateOf("") }

    val userModel by viewModel.repository.getUser().collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false)
    )

    LaunchedEffect(userModel.isLogin) {
        if (userModel.isLogin) {
            viewModel.loadSaka(userModel.token)
        }
    }

    val categories = listOf("Semua", "Sayur", "Buah", "Rempah", "Beras/Biji-bijian", "Lainnya")

    val filteredList = remember(sakaState, selectedCategoryFilter, searchText, selectedSortOption) {
        val list = (sakaState as? UiState.Success<List<SakaItem>>)?.data ?: emptyList()

        val filtered = list.filter { item ->
            val isCategoryMatch = if (selectedCategoryFilter == "Semua") true else item.category.equals(selectedCategoryFilter, ignoreCase = true)
            val isSearchMatch = if (searchText.isBlank()) true else item.name.contains(searchText, ignoreCase = true)
            isCategoryMatch && isSearchMatch
        }

        when (selectedSortOption) {
            "Termurah" -> filtered.sortedBy { it.price }
            "Termahal" -> filtered.sortedByDescending { it.price }
            else -> filtered
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F0E6))) {
        ProductTopBar(
            navController = navController,
            searchText = searchText,
            onSearchTextChange = { searchText = it }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                val sortIcon = when (selectedSortOption) {
                    "Termurah" -> Icons.Default.ArrowUpward
                    "Termahal" -> Icons.Default.ArrowDownward
                    else -> Icons.AutoMirrored.Filled.Sort
                }

                val sortColor = if (selectedSortOption == "Default") MaterialTheme.colorScheme.primary else Color.Red

                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = sortIcon,
                        contentDescription = "Urutkan Harga",
                        tint = sortColor
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Paling Relevan (Terbaru)") },
                        onClick = { selectedSortOption = "Default"; showSortMenu = false },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Harga Terendah") },
                        onClick = { selectedSortOption = "Termurah"; showSortMenu = false },
                        leadingIcon = { Icon(Icons.Default.ArrowUpward, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Harga Tertinggi") },
                        onClick = { selectedSortOption = "Termahal"; showSortMenu = false },
                        leadingIcon = { Icon(Icons.Default.ArrowDownward, null) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categories.forEach { category ->
                    CategoryChip(
                        label = category,
                        isSelected = category == selectedCategoryFilter,
                        onClick = { selectedCategoryFilter = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (sakaState) {
            UiState.Idle, UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(text = "Gagal memuat produk: ${(sakaState as UiState.Error).errorMessage}", color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Tidak ada produk ditemukan.", fontWeight = FontWeight.Bold)
                            Text("Coba ubah filter atau kata kunci.", style = MaterialTheme.typography.bodySmall)
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
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

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
            IconButton(onClick = { navController.navigate(Screen.Cart.route) }) {
                Icon(Icons.Default.ShoppingCart, contentDescription = "Keranjang")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

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
            .height(40.dp)
            .clickable { onClick(label) }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
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