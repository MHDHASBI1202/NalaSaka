package com.example.nalasaka.ui.screen.home

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.R
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.theme.BurntOrangeish
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.HomeViewModel
import com.example.nalasaka.ui.viewmodel.UiState

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    val sakaState by viewModel.sakaState.collectAsState()
    val userModel by authViewModel.userSession.collectAsState(
        initial = com.example.nalasaka.data.pref.UserModel("", "", "", false)
    )

    var showPromoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userModel.isLogin) {
        if (userModel.isLogin) {
            viewModel.loadSaka(userModel.token)
        } else {
            navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
        }
    }

    var searchText by remember { mutableStateOf("") }
    val listSaka = (sakaState as? UiState.Success<List<SakaItem>>)?.data ?: emptyList()

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HomeHeader(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    onSearch = { navController.navigate(Screen.Produk.route) },
                    onBannerClick = {
                        if (userModel.isPromoUsed) {
                            Toast.makeText(context, "Mohon maaf Yang Mulia, promo ini sudah Anda gunakan dan telah hangus.", Toast.LENGTH_LONG).show()
                        } else if (userModel.isPromoClaimed) {
                            Toast.makeText(context, "Promo aktif! Diskon akan otomatis terpasang saat Yang Mulia checkout.", Toast.LENGTH_LONG).show()
                        } else {
                            showPromoDialog = true
                        }
                    },
                    isPromoClaimed = userModel.isPromoClaimed,
                    isPromoUsed = userModel.isPromoUsed
                )
            }

            item {
                when (val state = sakaState) {
                    UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is UiState.Error -> Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Gagal memuat: ${state.errorMessage}", color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                    else -> Unit
                }
            }

            if (listSaka.isNotEmpty()) {
                item {
                    ProductCategorySection(
                        title = "Promo Terbaik Minggu Ini",
                        sakaList = listSaka.filter { it.discountPrice != null && it.discountPrice > 0 }.take(5).ifEmpty { listSaka.take(5) },
                        onClickItem = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                    )
                }

                item {
                    ProductCategorySection(
                        title = "Flash Sale",
                        sakaList = listSaka.shuffled().take(5),
                        onClickItem = { sakaId -> navController.navigate(Screen.Detail.createRoute(sakaId)) }
                    )
                }
            }
        }

        if (showPromoDialog) {
            AlertDialog(
                onDismissRequest = { showPromoDialog = false },
                title = { Text("🎉 Klaim Promo Spesial!") },
                text = {
                    Column {
                        Text("Selamat Yang Mulia!")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Anda berhak mendapatkan potongan langsung sebesar:")
                        Text(
                            "Rp 20.000",
                            style = MaterialTheme.typography.headlineSmall,
                            color = BurntOrangeish,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Untuk transaksi berikutnya.")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            authViewModel.claimPromo()
                            showPromoDialog = false
                        }
                    ) {
                        Text("Klaim Sekarang", color = BurntOrangeish)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPromoDialog = false }) {
                        Text("Nanti Saja", color = Color.Gray)
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun HomeHeader(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    onBannerClick: () -> Unit,
    isPromoClaimed: Boolean,
    isPromoUsed: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onBannerClick() }
        ) {
            Image(
                painter = painterResource(id = R.drawable.banner),
                contentDescription = "Promo Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(24.dp)
            ) {
                if (isPromoUsed) {
                    Surface(
                        color = Color.Gray,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "SUDAH DIGUNAKAN",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Promo Telah\nAnda Manfaatkan",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    )
                } else if (isPromoClaimed) {
                    Surface(
                        color = Color.Green,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "PROMO AKTIF",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Diskon Rp 20.000\nSiap Digunakan!",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    )
                } else {
                    Surface(
                        color = Color.Red,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "DISKON USER BARU",
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Potongan Rp 20.000\nUntuk Semua Buah!",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "(Ketuk banner untuk klaim)",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Box(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = { Text("Cari sayur atau buah segar...") },
                leadingIcon = {
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch()
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                )
            )
        }
    }
}

@Composable
fun ProductCategorySection(
    title: String,
    sakaList: List<SakaItem>,
    onClickItem: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
            TextButton(onClick = { }) {
                Text("Lihat Semua", color = BurntOrangeish)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
            .width(150.dp)
            .height(230.dp)
            .clickable { onClick(saka.id) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0))
            ) {
                AsyncImage(
                    model = saka.photoUrl,
                    contentDescription = saka.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = saka.name,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = saka.category,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.weight(1f))

            if (saka.discountPrice != null && saka.discountPrice > 0) {
                Text(
                    text = formatRupiah(saka.discountPrice),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Red,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = formatRupiah(saka.price),
                    style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough),
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            } else {
                Text(
                    text = formatRupiah(saka.price),
                    style = MaterialTheme.typography.titleSmall,
                    color = BurntOrangeish,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}