package com.example.nalasaka.ui.screen.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape // Import Shape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle // Import Icon Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight // Import Font Weight
import androidx.compose.ui.text.style.TextAlign // Import Text Align
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog // Import Dialog
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

    // State untuk Dropdown Pembayaran
    var expandedPayment by remember { mutableStateOf(false) }
    val paymentMethods = listOf("CASH" to "Bayar Tunai (COD)", "TRANSFER" to "Transfer Bank", "EWALLET" to "E-Wallet (Dana/OVO)")
    var selectedPaymentLabel by remember { mutableStateOf(paymentMethods[0].second) }

    // [BARU] State untuk Pop-up Sukses Checkout
    var showCheckoutSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    LaunchedEffect(checkoutState) {
        if (checkoutState is UiState.Success) {
            // [UPDATE] Jangan navigasi langsung, tapi tampilkan dialog
            showCheckoutSuccessDialog = true
            viewModel.resetCheckoutState()
        } else if (checkoutState is UiState.Error) {
            snackbarHostState.showSnackbar((checkoutState as UiState.Error).errorMessage)
            viewModel.resetCheckoutState()
        }
    }

    // [BARU] Render Custom Dialog Checkout Sukses
    if (showCheckoutSuccessDialog) {
        CheckoutSuccessDialog(
            onDismiss = {
                // Jika tutup, user tetap di halaman Cart (yang sekarang kosong)
                showCheckoutSuccessDialog = false
            },
            onGoToOrders = {
                // Jika pilih "Lihat Pesanan", navigasi ke history
                showCheckoutSuccessDialog = false
                navController.navigate(Screen.TransactionHistory.route)
            }
        )
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
                Surface(shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        // --- BAGIAN PILIH PEMBAYARAN ---
                        Text("Metode Pembayaran:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = expandedPayment,
                            onExpandedChange = { expandedPayment = !expandedPayment },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedPaymentLabel,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPayment) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedPayment,
                                onDismissRequest = { expandedPayment = false }
                            ) {
                                paymentMethods.forEach { (code, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            selectedPaymentLabel = label
                                            viewModel.paymentMethod.value = code // Update ViewModel
                                            expandedPayment = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- BARIS TOTAL & TOMBOL ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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

// [BARU] Composable Dialog Sukses Checkout
@Composable
fun CheckoutSuccessDialog(
    onDismiss: () -> Unit,
    onGoToOrders: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Ikon Centang Besar
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50), // Hijau Sukses
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Checkout Berhasil!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pesanan Anda telah diterima dan sedang diproses oleh penjual.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Tutup (Tetap di halaman ini)
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Tutup", color = MaterialTheme.colorScheme.primary)
                    }

                    // Tombol Ke Riwayat Pesanan
                    Button(
                        onClick = onGoToOrders,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Pesanan")
                    }
                }
            }
        }
    }
}