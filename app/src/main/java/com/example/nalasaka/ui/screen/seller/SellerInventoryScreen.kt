package com.example.nalasaka.ui.screen.seller

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.ProductGridItem
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.SellerViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerInventoryScreen(
    navController: NavHostController,
    viewModel: SellerViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val myProductsState by viewModel.myProductsState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMyProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stok Produk Saya", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = myProductsState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                is UiState.Success -> {
                    val products = state.data
                    if (products.isEmpty()) {
                        Text("Anda belum mengunggah produk apapun.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(products) { saka ->
                                ProductGridItem(
                                    saka = saka,
                                    onClick = { navController.navigate(Screen.Detail.createRoute(saka.id)) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}