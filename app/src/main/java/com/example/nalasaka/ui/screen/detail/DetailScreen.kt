package com.example.nalasaka.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // <-- PENTING: Mengimpor semua ikon Filled
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.theme.BurntOrangeish
import com.example.nalasaka.ui.theme.LightBackground
import com.example.nalasaka.ui.theme.LightSecondary
import com.example.nalasaka.ui.viewmodel.DetailViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import com.example.nalasaka.ui.navigation.Screen // Import Screen untuk navigasi

// --- Komponen Produk Serupa (Simulasi Item Card Sesuai Gambar) ---
@Composable
fun SuggestedSakaItem(
    saka: SakaItem,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Mereplikasi tampilan kartu produk serupa
    Column(
        modifier = modifier
            .width(110.dp)
            .clickable { onClick(saka.id) }
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        // Frame/Placeholder Gambar Produk Serupa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Kita gunakan AsyncImage jika tersedia, atau Box jika tidak
            AsyncImage(
                model = saka.photoUrl,
                contentDescription = saka.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE0E0E0)), // Warna abu-abu muda di gambar
                contentScale = ContentScale.Crop // Gunakan Crop agar pas di frame
            )
        }

        // Detail Teks
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(
                text = saka.name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${formatRupiah(saka.price)}", // Tampilkan harga yang diformat
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            )
        }
    }
}
// --- Akhir Komponen Produk Serupa ---


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    sakaId: String,
    navController: NavHostController,
    viewModel: DetailViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val sakaDetailState by viewModel.sakaDetailState.collectAsState()
    // Ambil state produk serupa
    val relatedProductsState by viewModel.relatedProductsState.collectAsState()

    LaunchedEffect(sakaId) {
        viewModel.loadSakaDetail(sakaId)
    }

    Scaffold(
        topBar = {
            // TopAppBar Sederhana dengan Tombol Kembali (panah bawah dari gambar)
            TopAppBar(
                title = { /* Kosongkan Title */ },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        // Menggunakan ikon panah ke bawah sesuai dengan gambar
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        },
        // Container color diatur menjadi LightBackground
        containerColor = LightBackground,

        // --- BOTTOM ACTION BAR (SESUAI GAMBAR) ---
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                containerColor = Color.White, // Latar belakang putih sesuai gambar
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Kiri: Icon Love (Favorite) dan Cart
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { /* Handle Favorite Click */ }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Color.Black)
                        }
                        // Icon Keranjang (ShoppingCart)
                        IconButton(onClick = { /* Handle Cart Click */ }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Add to Cart", tint = Color.Black)
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    // Kanan: Tombol Tambah (Primary Button)
                    PrimaryButton(
                        text = "Tambah",
                        onClick = { /* Handle Tambah ke Keranjang */ },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightSecondary)
                    )
                }
            }
        }
        // --- AKHIR BOTTOM ACTION BAR ---

    ) { paddingValues ->
        when (val state = sakaDetailState) {
            is UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Success -> {
                val saka = state.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- 1. AREA GAMBAR (Sesuai dengan frame di gambar) ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp) // Tinggi frame gambar
                            .background(Color(0xFFE0E0E0)), // Latar belakang frame
                        contentAlignment = Alignment.Center
                    ) {
                        // Gambar produk (AsyncImage)
                        AsyncImage(
                            model = saka.photoUrl,
                            contentDescription = saka.name,
                            // Tidak menggunakan clip agar gambar penuh di dalam Box
                            contentScale = ContentScale.Fit
                        )
                    }

                    // --- 2. DETAIL UTAMA (Harga, Nama, Rating, Deskripsi) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .background(LightBackground), // Latar belakang LightBackground
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Harga (Rp21.800: font besar, BurntOrangeish)
                        Text(
                            text = formatRupiah(saka.price ?: 0),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = BurntOrangeish
                            )
                        )
                        // Nama Produk (Jeruk: font besar)
                        Text(
                            text = saka.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        )
                        // Kuantitas (1.1 kg/pack: font kecil)
                        Text(
                            text = "1.1 kg/pack",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating (5/5 (1110 Ulasan))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.Star, contentDescription = "Rating Star", tint = BurntOrangeish, modifier = Modifier.size(20.dp))
                            Text(
                                text = "5/5 (1110 Ulasan)",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Deskripsi (Tekstur daging buahnya...)
                        Text(
                            text = saka.description,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, color = Color.Black)
                        )
                    }

                    // --- 3. PRODUK SERUPA (REAL DATA DARI API) ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightBackground)
                            .padding(top = 8.dp, bottom = 16.dp),
                    ) {
                        Text(
                            text = "Produk Serupa",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Tampilkan list dari API
                        when(val relatedState = relatedProductsState) {
                            is UiState.Success -> {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(relatedState.data) { item ->
                                        // Gunakan data real dari item
                                        SuggestedSakaItem(saka = item, onClick = {
                                            // Navigasi ke detail produk tersebut
                                            navController.navigate(Screen.Detail.createRoute(item.id))
                                        })
                                    }
                                }
                            }
                            is UiState.Loading -> {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                            is UiState.Error -> {
                                Text(
                                    text = "Gagal memuat rekomendasi.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Red,
                                    modifier = Modifier.padding(horizontal = 24.dp)
                                )
                            }
                            else -> { /* Kosong */ }
                        }
                    }
                }
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = "Gagal memuat detail produk: ${state.errorMessage}", color = MaterialTheme.colorScheme.error) }
        }
    }
}