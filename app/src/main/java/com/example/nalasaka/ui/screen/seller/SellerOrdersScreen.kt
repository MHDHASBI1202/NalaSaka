package com.example.nalasaka.ui.screen.seller

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.viewmodel.SellerViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.items // Pastikan ini ada untuk "items(state.data)"
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrdersScreen(
    navController: NavHostController,
    viewModel: SellerViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val ordersState by viewModel.ordersState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadSellerOrders() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Daftar Pesanan Pembeli") })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = ordersState) {
                is UiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is UiState.Success -> {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        items(state.data) { order ->
                            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                        Text("No Resi: ${order.resiNumber ?: "-"}", fontWeight = FontWeight.Bold)
                                        Surface(
                                            color = when(order.status) {
                                                "PENDING" -> Color.Yellow
                                                "DIPROSES" -> Color.Cyan
                                                "DIKIRIM" -> Color.Blue
                                                "SELESAI" -> Color.Green
                                                else -> Color.Red
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(order.status, modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text("Produk: ${order.productName}")
                                    Text("Jumlah: ${order.quantity}")
                                    Text("Tujuan: ${order.currentLocation}", color = Color.Gray, fontSize = 12.sp)

                                    Spacer(Modifier.height(12.dp))
                                    Row {
                                        Button(onClick = { viewModel.updateStatus(order.id, "DIPROSES") }) {
                                            Text("Proses")
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        OutlinedButton(onClick = { /* Dialog Input Resi */ }) {
                                            Text("Input Resi")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> Text("Tidak ada pesanan.", modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}