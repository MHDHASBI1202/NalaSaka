package com.example.nalasaka.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // Import LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.nalasaka.R // Import R
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.HomeViewModel // Import HomeViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory // Import ViewModelFactory
import com.example.nalasaka.data.remote.response.SakaItem as SakaData

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    // 1. Instansiasi HomeViewModel
    val homeViewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory.getInstance(LocalContext.current)
    )

    // 2. Ambil state dari ViewModel yang baru diinstansiasi
    val sakaState by homeViewModel.sakaState.collectAsState()
    val userModel by authViewModel.userSession.collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false, false) // Tambahkan isSeller default
    )

    // 3. LaunchedEffect diperbaiki untuk menggunakan homeViewModel
    LaunchedEffect(userModel.isLogin, userModel.token) {
        if (userModel.isLogin && userModel.token.isNotBlank()) {
            homeViewModel.loadSaka(userModel.token)
        } else {
            navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
        }
    }

    // Placeholder untuk Search
    var searchText by remember { mutableStateOf("") }

    val listSaka = (sakaState as? UiState.Success<List<SakaData>>)?.data ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp), // Berikan padding bawah
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HEADER (Banner dan Search Bar)
            item {
                HomeHeader(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    onSearch = { /* TODO: Implementasi Search */ },
                    onRefresh = { homeViewModel.loadSaka(userModel.token) } // Gunakan homeViewModel
                )
            }

            // Tampilkan Loading/Error State sebagai item terpusat
            item {
                when (val state = sakaState) {
                    UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is UiState.Error -> Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(text = "ERROR: ${state.errorMessage}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                    else -> Unit // Lanjut ke daftar produk di bawah
                }
            }

            // 2. Kategori Produk (Promo Terbaik Minggu Ini)
            item {
                if (listSaka.isNotEmpty()) {
                    ProductCategorySection(
                        title = "Promo Terbaik Minggu Ini",
                        sakaList = listSaka,
                        onClickItem = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                    )
                }
            }

            // 3. Kategori Produk (Flash Sale) - Duplikasi untuk tampilan
            item {
                if (listSaka.isNotEmpty()) {
                    ProductCategorySection(
                        title = "Flash Sale",
                        sakaList = listSaka.reversed(), // Contoh data berbeda
                        onClickItem = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                    )
                }
            }

            // 4. Kategori Produk (Last Chance)
            item {
                if (listSaka.isNotEmpty()) {
                    ProductCategorySection(
                        title = "Last Chance",
                        sakaList = listSaka,
                        onClickItem = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                    )
                }
            }
        }
    }
}

// ... (Komponen pendukung HomeHeader, ProductCategorySection, SakaCardHorizontal, SakaItemImagePlaceholder dipertahankan)

@Composable
fun HomeHeader(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp) // Tinggi Banner
            .background(Color(0xFFE0E0E0)) // Warna background placeholder
    ) {
        // Banner Promo (Menggunakan Image Placeholder dari file yang diupload)
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background), // Ganti dengan resource Promo.jpg jika sudah diimport
            contentDescription = "Promo Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay untuk Bilah Pencarian
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom // Tempatkan di bawah
        ) {
            // Placeholder Title/Info di Banner (Misal: Khasus Pengguna Baru)
            Text("KHUSUS PENGGUNA BARU", color = Color.White, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                label = { Text("Cari Kebutuhanmu") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProductCategorySection(
    title: String,
    sakaList: List<SakaData>,
    onClickItem: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { /* TODO: Navigasi Lihat Semua */ }) {
                Text("Lihat Semua", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Gunakan LazyRow untuk daftar horizontal
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sakaList, key = { it.id }) { saka ->
                // Menggunakan SakaItem yang dimodifikasi untuk tampilan horizontal/Card
                SakaCardHorizontal(saka = saka, onClick = onClickItem)
            }
        }
    }
}

@Composable
fun SakaCardHorizontal(
    saka: SakaData,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp) // Ukuran Card horizontal
            .height(220.dp)
            .clickable { onClick(saka.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Gambar Produk
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)) // Placeholder warna abu-abu
            ) {
                // Di sini Anda bisa menggunakan AsyncImage untuk menampilkan saka.photoUrl
                // Untuk sementara, kita pakai placeholder warna atau gambar dummy
                SakaItemImagePlaceholder(saka.photoUrl)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nama & Deskripsi (Disimpan ke dalam Column)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = saka.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${(saka.price / 1000.0).toString()} kg/pack", // Contoh dummy weight
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Harga Produk
                Text(
                    text = com.example.nalasaka.ui.components.formatRupiah(saka.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, // Burnt Orangeish
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SakaItemImagePlaceholder(url: String) {
    // Implementasi Coil AsyncImage harusnya ada di sini,
    // tetapi untuk menjaga kesamaan dengan gambar Yang Mulia,
    // kita akan menggunakan placeholder sementara.
    // Jika Anda telah mengintegrasikan Coil, gunakan:
    /*
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
    */
    // Placeholder menggunakan Image bawaan Android untuk logo
    Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "Product Image Placeholder",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}