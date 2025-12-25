    package com.example.nalasaka.ui.screen.history

    import android.content.Context
    import android.content.Intent
    import android.net.Uri
    import androidx.activity.compose.rememberLauncherForActivityResult
    import androidx.activity.result.contract.ActivityResultContracts
    import androidx.activity.result.launch
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.CameraAlt
    import androidx.compose.material.icons.filled.CheckCircle
    import androidx.compose.material.icons.filled.ConfirmationNumber
    import androidx.compose.material.icons.filled.LocationOn
    import androidx.compose.material.icons.filled.Refresh
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.navigation.NavHostController
    import coil.compose.AsyncImage
    import com.example.nalasaka.data.remote.response.TransactionItem
    import com.example.nalasaka.ui.components.formatRupiah
    import com.example.nalasaka.ui.viewmodel.TransactionViewModel
    import com.example.nalasaka.ui.viewmodel.UiState
    import com.example.nalasaka.ui.viewmodel.ViewModelFactory
    import com.example.nalasaka.ui.viewmodel.CartViewModel
    import kotlin.toString

    @Composable
    fun TransactionHistoryScreen(
        navController: NavHostController,
        viewModel: TransactionViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context)),
        cartViewModel: CartViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
    ) {
        val context = LocalContext.current
        val historyState by viewModel.historyState.collectAsState()


        LaunchedEffect(Unit) {
            viewModel.getHistory()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Pesanan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (val state = historyState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada transaksi.")
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.data) { transaction ->
                                TransactionCard(
                                    item = transaction,
                                    onReorder = { sakaId ->
                                        cartViewModel.addToCartFromDetail(sakaId)
                                        android.widget.Toast.makeText(context, "Produk masuk keranjang", android.widget.Toast.LENGTH_SHORT).show()
                                        navController.navigate(com.example.nalasaka.ui.navigation.Screen.Cart.route)
                                    },
                                    onUpdateStatus = { id, newStatus ->
                                        viewModel.updateTransactionStatus(id.toInt(), newStatus)
                                    }
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {}
                else -> {}
            }
        }
    }

    @Composable
    fun TransactionCard(
        item: TransactionItem,
        onReorder: (String) -> Unit,
        onUpdateStatus: (String, String) -> Unit,
        context: Context = LocalContext.current
    ) {
        var showDetailDialog by remember { mutableStateOf(false) }
        var capturedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap ->
            if (bitmap != null) {
                capturedBitmap = bitmap
            }
        }
        if (showDetailDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDetailDialog = false
                    capturedBitmap = null
                },
                title = { Text("Konfirmasi Pesanan") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Produk: ${item.productName}", modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))

                        if (capturedBitmap != null) {
                            Text("Pratinjau Bukti:", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            AsyncImage(
                                model = capturedBitmap,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                            )
                            TextButton(onClick = { capturedBitmap = null }) {
                                Text("Hapus & Foto Ulang", color = Color.Red)
                            }
                        } else {
                            if (item.shippingMethod == "Ambil ke Toko") {
                                Text("Kode Ambil: ${item.pickupCode}", fontWeight = FontWeight.Bold, color = Color.Blue)
                                Spacer(Modifier.height(8.dp))
                            }

                            Text("Ambil foto bukti untuk menyelesaikan pesanan:")
                            Button(
                                onClick = { cameraLauncher.launch() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                            ) {
                                Icon(Icons.Default.CameraAlt, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Buka Kamera")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onUpdateStatus(item.id, "SELESAI")
                            showDetailDialog = false
                            capturedBitmap = null
                        },
                        enabled = capturedBitmap != null
                    ) {
                        Text("Selesaikan Pesanan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDetailDialog = false }) { Text("Batal") }
                }
            )
        }

        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tanggal: ${item.date}", fontSize = 12.sp, color = Color.Gray)
                    Surface(color = getStatusColor(item.status), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            item.status,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp),
                            fontSize = 10.sp
                        )
                    }
                }
                Row {
                    AsyncImage(
                        model = item.productImage,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(item.productName, fontWeight = FontWeight.Bold)
                        Text(formatRupiah(item.price), color = Color(0xFFE67E22))
                    }
                }

                if (item.shippingMethod == "Ambil ke Toko") {
                    if (item.status != "SELESAI" && item.status != "BATAL") {
                        Button(
                            onClick = { showDetailDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.ConfirmationNumber, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Lihat Detail")
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                val label = item.storeName ?: item.productName ?: "Toko Penjual"
                                val gmmIntentUri = Uri.parse("geo:${item.latitude},${item.longitude}?q=${item.latitude},${item.longitude}($label)")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocationOn, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Navigasi Ke Toko")
                        }
                    } else {
                        Button(onClick = { onReorder(item.sakaId) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Pesan Lagi")
                        }
                    }

                } else {
                    if (item.status == "DIKIRIM") {
                        Button(
                            onClick = { showDetailDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                        ) {
                            Icon(Icons.Default.CheckCircle, null)
                            Spacer(Modifier.width(8.dp))
                            Text("KONFIRMASI BARANG SAMPAI")
                        }
                    } else if (item.status == "SELESAI" || item.status == "BATAL" || item.status == "DIPROSES") {
                        // Selain itu tampilkan pesan ulang atau info resi
                        if (item.status != "DIPROSES") {
                            Button(onClick = { onReorder(item.sakaId) }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Refresh, null)
                                Spacer(Modifier.width(8.dp))
                                Text("PESAN ULANG")
                            }
                        } else {
                            Text("Pesanan sedang disiapkan oleh penjual", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    fun getStatusColor(status: String): Color {
        return when (status) {
            "PENDING" -> Color(0xFFFFA000)
            "DIPROSES" -> Color(0xFF1976D2)
            "DIKIRIM" -> Color(0xFF7B1FA2)
            "SELESAI" -> Color(0xFF388E3C)
            "BATAL" -> Color(0xFFD32F2F)
            else -> Color.Gray
        }
    }