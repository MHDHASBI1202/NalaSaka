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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage // PENTING: Import Coil
import com.example.nalasaka.R
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.HomeViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    viewModel: HomeViewModel
) {
    val sakaState by viewModel.sakaState.collectAsState()
    val userModel by authViewModel.userSession.collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false)
    )

    // Load data saat masuk halaman jika sudah login
    LaunchedEffect(userModel.isLogin) {
        if (userModel.isLogin) {
            viewModel.loadSaka(userModel.token)
        } else {
            navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
        }
    }

    // Placeholder untuk Search
    var searchText by remember { mutableStateOf("") }

    // Ambil data list dari state
    val listSaka = (sakaState as? UiState.Success<List<SakaItem>>)?.data ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp), // Berikan padding bawah agar tidak tertutup BottomBar
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. HEADER (Banner dan Search Bar)
            item {
                HomeHeader(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    onSearch = { /* TODO: Implementasi Search ke ProductScreen */ },
                    onRefresh = { viewModel.loadSaka(userModel.token) }
                )
            }

            // Tampilkan Loading/Error State
            item {
                when (val state = sakaState) {
                    UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is UiState.Error -> Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Gagal memuat: ${state.errorMessage}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                    else -> Unit
                }
            }

            // Jika data ada, tampilkan section produk
            if (listSaka.isNotEmpty()) {
                // 2. Kategori Produk (Promo Terbaik Minggu Ini)
                // Kita ambil 5 data pertama sebagai contoh
                item {
                    ProductCategorySection(
                        title = "Promo Terbaik Minggu Ini",
                        sakaList = listSaka.take(5),
                        onClickItem = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                    )
                }

                // 3. Kategori Produk (Flash Sale)
                // Kita acak urutannya (shuffled) dan ambil 5 untuk variasi
                item {
                    ProductCategorySection(
                        title = "Flash Sale",
                        sakaList = listSaka.shuffled().take(5),
                        onClickItem = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                    )
                }

                // 4. Kategori Produk (Last Chance)
                // Kita ambil 5 data terakhir
                item {
                    ProductCategorySection(
                        title = "Last Chance",
                        sakaList = listSaka.takeLast(5),
                        onClickItem = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                    )
                }
            }
        }
    }
}

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
        // Banner Promo (Static Resource)
        // Catatan: Jika ingin banner dari database, perlu buat API khusus Banner.
        // Untuk saat ini kita gunakan resource gambar statis aplikasi.
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
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
            // Teks Info di Banner
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
    sakaList: List<SakaItem>,
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
            TextButton(onClick = { /* TODO: Navigasi Lihat Semua ke ProductScreen */ }) {
                Text("Lihat Semua", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Gunakan LazyRow untuk daftar horizontal
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sakaList, key = { it.id }) { saka ->
                SakaCardHorizontal(saka = saka, onClick = onClickItem)
            }
        }
    }
}

@Composable
fun SakaCardHorizontal(
    saka: SakaItem,
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
            // Gambar Produk (Menggunakan AsyncImage dari Coil)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0E0E0)) // Placeholder warna abu-abu saat loading
            ) {
                // [PERBAIKAN UTAMA] Menggunakan AsyncImage untuk memuat URL dari database
                AsyncImage(
                    model = saka.photoUrl,
                    contentDescription = saka.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    // Gambar fallback jika URL error atau loading gagal
                    error = painterResource(id = R.drawable.ic_launcher_foreground),
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nama & Deskripsi
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = saka.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                // Menampilkan Kategori sebagai info tambahan
                Text(
                    text = "Kategori: ${saka.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Harga Produk
                Text(
                    text = formatRupiah(saka.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary, // Burnt Orangeish
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}