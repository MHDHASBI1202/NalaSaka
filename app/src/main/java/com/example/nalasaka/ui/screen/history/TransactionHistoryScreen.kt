package com.example.nalasaka.ui.screen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.data.remote.response.TransactionItem
import com.example.nalasaka.ui.components.SakaBottomBar // <--- Penting: Import BottomBar
import com.example.nalasaka.ui.viewmodel.TransactionViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@Composable
fun TransactionHistoryScreen(
    navController: NavHostController,
    viewModel: TransactionViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val historyState by viewModel.historyState.collectAsState()

    // Load data saat pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.getHistory()
    }

    // --- PERUBAHAN DI SINI: Gunakan Scaffold ---
    Scaffold(
        bottomBar = {
            SakaBottomBar(navController = navController) // Memasang Nav Bar di bawah
        }
    ) { innerPadding -> // Padding otomatis agar konten tidak tertutup Nav Bar

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Terapkan padding dari Scaffold
                .padding(16.dp)
        ) {
            Text(
                text = "Riwayat Transaksi",
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
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.data) { transaction ->
                                TransactionCard(transaction, onReorder = {
                                    viewModel.checkoutItem(transaction.id)
                                })
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    // Tampilkan pesan error yang lebih ramah
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Gagal memuat data: ${state.errorMessage}", color = Color.Red)
                    }
                }

                UiState.Idle -> TODO()
            }
        }
    }
}

@Composable
fun TransactionCard(transaction: TransactionItem, onReorder: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Tanggal & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = transaction.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                Surface(
                    color = getStatusColor(transaction.status),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = transaction.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Content: Gambar & Detail
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = transaction.productImage,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = transaction.productName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Total: Rp ${transaction.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp))

            // Pelacakan Lokasi (Fitur Logistik)
            Row(verticalAlignment = Alignment.Top) {
                Text("Lokasi: ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text(transaction.tracking.location, style = MaterialTheme.typography.bodySmall)
            }
            Text("Resi: ${transaction.tracking.resi}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onReorder,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Pesan Ulang")
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