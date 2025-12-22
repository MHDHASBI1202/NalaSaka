package com.example.nalasaka.ui.screen.seller

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.data.remote.response.DailySalesItem
import com.example.nalasaka.data.remote.response.ProductSalesStat
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.theme.BurntOrangeish
import com.example.nalasaka.ui.theme.DeepMoss
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.ProfileViewModel
import com.example.nalasaka.ui.viewmodel.SellerViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    navController: NavHostController,
    sellerViewModel: SellerViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context)),
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context)),
    profileViewModel: ProfileViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val userModel by authViewModel.userSession.collectAsState(initial = UserModel("", "", "", false))
    val profileState by profileViewModel.profileState.collectAsState()
    val statsState by sellerViewModel.statsState.collectAsState()

    // NEW: Collect state broadcast
    val broadcastState by sellerViewModel.broadcastState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        sellerViewModel.loadDashboardData()
        profileViewModel.loadUserProfile()
    }

    // NEW: Handle feedback broadcast (Toast)
    LaunchedEffect(broadcastState) {
        when (val state = broadcastState) {
            is UiState.Success -> {
                Toast.makeText(context, state.data, Toast.LENGTH_LONG).show()
                sellerViewModel.resetBroadcastState()
            }
            is UiState.Error -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_LONG).show()
                sellerViewModel.resetBroadcastState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard Toko", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                actions = {
                    IconButton(onClick = {
                        if (userModel.token.isNotEmpty()) {
                            // URL Endpoint Laravel disesuaikan dengan koneksi
                            val reportUrl = "http://10.0.2.2/nalasaka-api/public/api/seller/report/download?token=${userModel.token}"
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = reportUrl.toUri()
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Browser tidak ditemukan", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Print,
                            contentDescription = "Cetak Laporan",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // --- HEADER TOKO ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DeepMoss),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Store Icon",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "Halo, Seller!", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userModel.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            val isVerified = (profileState as? UiState.Success)?.data?.verificationStatus == "verified"
                            if (isVerified) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Seller",
                                    tint = Color(0xFF07C91F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- MENU KELOLA ---
            Text(
                text = "Menu Kelola",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                SellerMenuCard(
                    title = "Upload Barang",
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(Screen.AddSaka.route) }
                )

                // [UPDATE] Tombol Siarkan Promo
                SellerMenuCard(
                    title = if (broadcastState is UiState.Loading) "Mengirim..." else "Siarkan Promo",
                    icon = Icons.Default.Campaign,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        if (broadcastState !is UiState.Loading) {
                            sellerViewModel.broadcastPromo()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                SellerMenuCard(
                    title = "Stok Produk",
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.fillMaxWidth(0.5f),
                    onClick = { navController.navigate(Screen.SellerInventory.route) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- STATISTIK RINGKASAN ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ringkasan Total", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val stats = (statsState as? UiState.Success)?.data
                        StatItem(label = "Terjual", value = stats?.sold?.toString() ?: "-")
                        StatItem(label = "Pendapatan", value = if (stats != null) formatRupiah(stats.revenue) else "-")
                        StatItem(label = "Produk", value = stats?.productCount?.toString() ?: "-")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- GRAFIK PENJUALAN HARIAN ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = BurntOrangeish)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tren Penjualan (7 Hari)", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    when (val state = statsState) {
                        is UiState.Success -> {
                            SimpleBarChart(dailyData = state.data.dailySales)
                        }
                        is UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- PERFORMA PRODUK (DROPDOWN) ---
            when (val state = statsState) {
                is UiState.Success -> {
                    ProductPerformanceCard(products = state.data.productPerformance)
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProductPerformanceCard(products: List<ProductSalesStat>) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = null,
                        tint = DeepMoss
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Performa Barang", fontWeight = FontWeight.Bold)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                    if (products.isEmpty()) {
                        Text(
                            text = "Belum ada produk yang terjual.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        products.forEachIndexed { index, product ->
                            ProductPerformanceItem(product, index + 1)
                            if (index < products.size - 1) {
                                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductPerformanceItem(product: ProductSalesStat, rank: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (rank <= 3) BurntOrangeish else Color.Gray,
            modifier = Modifier.width(30.dp)
        )

        AsyncImage(
            model = product.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = "${product.soldQty} Terjual",
                style = MaterialTheme.typography.labelSmall,
                color = DeepMoss
            )
        }

        Text(
            text = formatRupiah(product.totalRevenue),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SimpleBarChart(dailyData: List<DailySalesItem>) {
    if (dailyData.isEmpty()) {
        Text("Belum ada data penjualan minggu ini.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        return
    }

    val maxAmount = dailyData.maxOfOrNull { it.amount } ?: 1
    val scale = if (maxAmount == 0) 1 else maxAmount

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        dailyData.forEach { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                if (item.amount > 0) {
                    Text(
                        text = formatCompactNumber(item.amount),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                val barHeightFraction = (item.amount.toFloat() / scale.toFloat()).coerceIn(0.02f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(barHeightFraction)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (item.amount > 0) BurntOrangeish else Color.LightGray.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = item.day,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepMoss
                )
            }
        }
    }
}

fun formatCompactNumber(number: Int): String {
    return when {
        number >= 1000000 -> "${String.format("%.1f", number / 1000000.0)}M"
        number >= 1000 -> "${number / 1000}k"
        else -> number.toString()
    }
}

@Composable
fun SellerMenuCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DeepMoss)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}