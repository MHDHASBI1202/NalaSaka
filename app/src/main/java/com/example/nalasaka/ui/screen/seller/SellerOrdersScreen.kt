package com.example.nalasaka.ui.screen.seller

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.example.nalasaka.data.remote.response.OrderItem
import com.example.nalasaka.ui.viewmodel.SellerOrderViewModel
import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrderScreen(
    navController: NavHostController,
    viewModel: SellerOrderViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val context = LocalContext.current
    val state by viewModel.ordersState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getSellerOrders()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pesanan Masuk Toko", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        when (val result = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Success -> {
                LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                    items(result.data) { order ->
                        SellerOrderCard(
                            order = order,
                            onUpdate = { status -> viewModel.updateStatus(order.id, status) }
                        )
                    }
                }
            }
            is UiState.Error -> Text("Error: ${result.message}")
            else -> {}
        }
    }
}

@Composable
fun SellerOrderCard(order: OrderItem, onUpdate: (String) -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Pembeli: ${order.buyerName}", fontWeight = FontWeight.Bold)
            Text("Produk: ${order.productName} (x${order.quantity})")

            Spacer(Modifier.height(4.dp))
            Text("Alamat: ${order.fullAddress}", fontSize = 13.sp, color = Color.DarkGray)

            Text("Status: ${order.status}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            if (order.shippingMethod == "Diantar") {
                Button(
                    onClick = {
                        if (order.latitude != null && order.longitude != null) {
                            val uri = Uri.parse("google.navigation:q=${order.latitude},${order.longitude}")
                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.Map, null)
                    Text(" NAVIGASI KE RUMAH PEMBELI")
                }

                if (order.status == "PROSES") {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { onUpdate("DIKIRIM") }, modifier = Modifier.fillMaxWidth()) {
                        Text("KONFIRMASI PENGIRIMAN")
                    }
                }
            }

            if (order.shippingMethod == "Ambil ke Toko") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF9C4), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("KODE PENGAMBILAN", fontSize = 12.sp, fontWeight = FontWeight.Light)
                        Text(
                            text = order.pickupCode ?: "-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}