package com.example.nalasaka.ui.screen.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.data.remote.response.CartItem
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.CartViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavHostController,
    viewModel: CartViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val cartState by viewModel.cartState.collectAsState()
    val total by viewModel.totalPrice.collectAsState()
    val checkoutState by viewModel.checkoutState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    LaunchedEffect(checkoutState) {
        if (checkoutState is UiState.Success) {
            snackbarHostState.showSnackbar("Checkout Berhasil!")
            viewModel.resetCheckoutState()
            navController.navigate(Screen.TransactionHistory.route) // Pindah ke riwayat
        } else if (checkoutState is UiState.Error) {
            snackbarHostState.showSnackbar((checkoutState as UiState.Error).errorMessage)
            viewModel.resetCheckoutState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keranjang Belanja") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            if (cartState is UiState.Success && (cartState as UiState.Success).data.isNotEmpty()) {
                // PERBAIKAN DI SINI: Hapus parameter 'elevation'
                Surface(
                    shadowElevation = 8.dp,
                    // tonalElevation = 2.dp // Opsional: Tambahkan ini jika ingin efek warna Material 3
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Harga:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = formatRupiah(total),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Button(
                            onClick = { viewModel.checkout() },
                            enabled = checkoutState !is UiState.Loading
                        ) {
                            Text(if (checkoutState is UiState.Loading) "Memproses..." else "CHECKOUT")
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = cartState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Error -> Text(state.errorMessage, modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Text("Keranjang masih kosong.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(state.data) { item ->
                                CartItemCard(item,
                                    onAdd = { viewModel.updateQuantity(item, item.quantity + 1) },
                                    onMin = { viewModel.updateQuantity(item, item.quantity - 1) }
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

@Composable
fun CartItemCard(item: CartItem, onAdd: () -> Unit, onMin: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(2.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.photoUrl,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(formatRupiah(item.price), color = MaterialTheme.colorScheme.primary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMin) { Icon(Icons.Default.Remove, "Kurang") }
                Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onAdd) { Icon(Icons.Default.Add, "Tambah") }
            }
        }
    }
}