package com.example.nalasaka.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // Mengimpor semua ikon yang diperlukan
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.theme.DeepMoss
import com.example.nalasaka.ui.theme.LightBackground
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.HomeViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory

// Warna ikon gelap kustom untuk Bottom Bar
private val IconDark = Color(0xFF4C4C4C)

// --- Helper Composable: Promo Item Card (Horizontal Scroll) ---
@Composable
fun PromoItemCard(saka: SakaItem, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick(saka.id) }
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Image Placeholder (FOTO)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = saka.photoUrl,
                contentDescription = saka.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product Details
        Text(
            text = saka.name,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "0.6-0.8 kg/pack",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatRupiah(saka.price),
            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
            maxLines = 1
        )
    }
}

// --- Helper Composable: Featured Product Card (Vertical Grid) ---
@Composable
fun ProductGridCard(saka: SakaItem, onClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable { onClick(saka.id) }
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Image Placeholder (FOTO)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = saka.photoUrl,
                contentDescription = saka.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product Details
        Text(
            text = saka.name,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp),
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "0.6-0.8 kg/pack",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatRupiah(saka.price),
            style = MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
            maxLines = 1
        )
    }
}

// --- Bottom Navigation Bar (Sudah dimodifikasi untuk Tambah Produk & Profile) ---
@Composable
fun BottomNavBar(navController: NavHostController) {
    // Icons for Bottom Bar (Mengganti 'Produk' dengan 'Tambah')
    val items = listOf(
        Triple(Icons.Filled.Home, "Home", Screen.Home.route),
        Triple(Icons.Filled.Add, "Tambah", Screen.AddSaka.route), // FUNGSI TAMBAH PRODUK
        Triple(Icons.Filled.Percent, "Promo", "promo_route"),
        Triple(Icons.Filled.Receipt, "Pesanan", "pesanan_route"),
        Triple(Icons.Filled.Person, "Profil", Screen.Profile.route) // Rute Profile
    )

    BottomAppBar(
        containerColor = LightBackground, // Warna F4E2D0
        modifier = Modifier.height(70.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items.forEach { (icon, label, route) ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable {
                        // Navigasi ke Home, AddSaka, atau Profile.
                        if (route == Screen.Home.route || route == Screen.AddSaka.route || route == Screen.Profile.route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (label == "Home") DeepMoss else IconDark,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = if (label == "Home") DeepMoss else IconDark
                    )
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context)),
    viewModel: HomeViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val sakaState by viewModel.sakaState.collectAsState()

    val userModel by authViewModel.userSession.collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false)
    )

    // FUNGSI LOGOUT
    val onLogoutClick: () -> Unit = {
        authViewModel.logout()
    }

    LaunchedEffect(userModel.isLogin) {
        if (userModel.isLogin) {
            viewModel.loadSaka(userModel.token)
        } else {
            // Navigasi ke Welcome jika belum login
            navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
        }
    }

    Scaffold(
        // TOP BAR (REVERTED): Hanya berisi Home, Refresh, Logout, dan Ikon Akun (tanpa navigasi profil)
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .statusBarsPadding()
                    .background(DeepMoss), // Set background agar ikon terlihat jelas
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TENGAH: Spacer
                Spacer(modifier = Modifier.weight(1f))

                // KANAN: REFRESH, LOGOUT, AKUN
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ICON REFRESH
                    IconButton(onClick = {
                        viewModel.loadSaka(userModel.token) // FUNGSI REFRESH
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }

                    // ICON LOGOUT
                    IconButton(onClick = onLogoutClick) {
                        // FUNGSI LOGOUT
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                }
            }
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        containerColor = LightBackground
    ) { paddingValues ->
        when (val state = sakaState) {
            UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = "ERROR: ${state.errorMessage}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
            is UiState.Success -> {
                val listSaka = state.data

                // --- LOGIKA PENAMBAHAN ITEM DUMMY ---
                val baseList = if (listSaka.isEmpty()) {
                    listOf(
                        SakaItem(id = "base-0", name = "Nanas Madu", description = "Dummy base", photoUrl = "", price = 15000),
                        SakaItem(id = "base-1", name = "Bawang Merah", description = "Dummy base", photoUrl = "", price = 45000)
                    )
                } else {
                    listSaka
                }

                // Tambahkan item dummy hingga total mencapai minimal 12 item
                val targetSize = 12
                val extendedListSaka = if (baseList.size < targetSize) {
                    val dummyItems = List(targetSize - baseList.size) { index ->
                        val nameList = listOf("Mangga Harum", "Sawi Hijau", "Daging Ayam", "Wortel Segar", "Kentang Lokal")
                        val dummyName = nameList[index % nameList.size]
                        SakaItem(
                            id = "ext-dummy-${index}",
                            name = dummyName,
                            description = "Produk Pilihan Tambahan",
                            photoUrl = "",
                            price = (25000 + index * 1000)
                        )
                    }
                    baseList + dummyItems
                } else {
                    baseList
                }
                // --- AKHIR LOGIKA PENAMBAHAN ITEM DUMMY ---


                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = paddingValues,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // 1. BANNER PROMO UTAMA (Area Iklan Promo)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(DeepMoss)
                                .padding(16.dp)
                        ) {
                            // Konten Banner
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Teks Kiri
                                Text(
                                    text = "KHUSUS\nPENGGUNA\nBARU",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 40.sp
                                    )
                                )

                                // Gambar/Info Promo Kanan
                                Box(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary), // Warna Orange/BurntOrangeish
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Rp75RB\nCode AEIBT",
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    // 2. SEARCH BAR
                    item {
                        OutlinedTextField(
                            value = "",
                            onValueChange = { /* Tidak ada perubahan state */ },
                            placeholder = { Text("Cari Kebutuhanmu") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-30).dp) // Offset ke atas, menindih banner
                                .padding(horizontal = 16.dp)
                        )
                    }

                    // 3. PROMO TERBAIK MINGGU INI (Horizontal Scroll)
                    item {
                        Column(modifier = Modifier.fillMaxWidth().offset(y = (-20).dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Promo Terbaik Minggu ini",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                TextButton(onClick = { /* Handle Lihat Semua Click */ }) {
                                    Text("Lihat Semua", color = MaterialTheme.colorScheme.secondary)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Menggunakan 6 item pertama sebagai Promo
                                val promoItems = extendedListSaka.take(6)

                                items(promoItems) { saka ->
                                    PromoItemCard(
                                        saka = saka,
                                        onClick = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                                    )
                                }
                            }
                        }
                    }

                    // 4. PRODUK PILIHAN (Vertical Grid)
                    item {
                        Text(
                            text = "Produk Pilihan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Logika untuk Grid 2 Kolom menggunakan extendedListSaka
                    val pairs = extendedListSaka.chunked(2)
                    items(pairs) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { saka ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductGridCard(
                                        saka = saka,
                                        onClick = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                                    )
                                }
                            }
                            // Tambahkan Box kosong jika item ganjil
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Padding di bawah
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}