package com.example.nalasaka.ui.screen.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.StarHalf
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
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
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.data.remote.response.ReviewItem
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.theme.BurntOrangeish
import com.example.nalasaka.ui.theme.LightBackground
import com.example.nalasaka.ui.theme.LightSecondary
import com.example.nalasaka.ui.viewmodel.DetailViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun SuggestedSakaItem(
    saka: SakaItem,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(110.dp)
            .clickable { onClick(saka.id) }
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(Color.White)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = saka.photoUrl,
                contentDescription = saka.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE0E0E0)),
                contentScale = ContentScale.Crop
            )
        }

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
                text = formatRupiah(saka.price),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    sakaId: String,
    navController: NavHostController,
    viewModel: DetailViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context)),
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val sakaDetailState by viewModel.sakaDetailState.collectAsState()
    val relatedProductsState by viewModel.relatedProductsState.collectAsState()
    val reviewState by viewModel.reviewState.collectAsState()
    val submitReviewState by viewModel.submitReviewState.collectAsState()

    // Ambil data User Login
    val userSession by authViewModel.userSession.collectAsState(initial = com.example.nalasaka.data.pref.UserModel("","","",false))
    val currentUserId = userSession.userId

    var showReviewDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // State: Apakah user sudah pernah review?
    var hasReviewed by remember { mutableStateOf(false) }
    // State: Menyimpan data review user sebelumnya (untuk pre-fill dialog edit)
    var myReview: ReviewItem? by remember { mutableStateOf(null) }

    LaunchedEffect(sakaId) {
        viewModel.loadSakaDetail(sakaId)
    }

    // Effect untuk memantau hasil submit
    LaunchedEffect(submitReviewState) {
        when (val state = submitReviewState) {
            is UiState.Success -> {
                showReviewDialog = false
                val msg = if (hasReviewed) "Ulasan diperbarui!" else "Ulasan terkirim!"
                snackbarHostState.showSnackbar(msg)
                viewModel.resetSubmitState()
                viewModel.loadSakaDetail(sakaId) // Reload data
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.errorMessage)
                viewModel.resetSubmitState()
            }
            else -> {}
        }
    }

    if (showReviewDialog) {
        AddReviewDialog(
            onDismiss = { showReviewDialog = false },
            onSubmit = { rating, comment ->
                viewModel.submitReview(sakaId, rating, comment, null)
            },
            isLoading = submitReviewState is UiState.Loading,
            initialRating = myReview?.rating ?: 0,
            initialComment = myReview?.comment ?: "",
            isEdit = hasReviewed
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
            )
        },
        containerColor = LightBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                containerColor = Color.White,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite", tint = Color.Black)
                        }
                        IconButton(onClick = { }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Add to Cart", tint = Color.Black)
                        }
                    }
                    Spacer(Modifier.width(16.dp))
                    PrimaryButton(
                        text = "Beli Sekarang",
                        onClick = {
                            viewModel.viewModelScope.launch {
                                // TODO: Implementasi Checkout
                            }
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LightSecondary)
                    )
                }
            }
        }
    ) { paddingValues ->
        when (val state = sakaDetailState) {
            is UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Success -> {
                val saka = state.data
                val reviewData = (reviewState as? UiState.Success)?.data
                val averageRating = reviewData?.averageRating ?: 0.0
                val totalReviews = reviewData?.totalReviews ?: 0
                val listReviews = reviewData?.reviews ?: emptyList()

                LaunchedEffect(listReviews, currentUserId) {
                    myReview = listReviews.find { it.userId == currentUserId }
                    hasReviewed = myReview != null
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gambar Produk
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = saka.photoUrl,
                            contentDescription = saka.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Detail Teks
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .background(LightBackground),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatRupiah(saka.price),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BurntOrangeish
                            )
                        )

                        // NAMA PRODUK & BADGE VERIFIED (JIKA ADA)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = saka.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                ),
                                modifier = Modifier.weight(1f, fill = false) // Agar tidak menabrak badge
                            )

                            // [FITUR YANG MULIA] Tampilkan Centang Hijau jika Seller Verified
                            if (saka.isSellerVerified) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Verified Seller",
                                    tint = Color(0xFF4CAF50), // Hijau
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Text(
                            text = "Kategori: ${saka.category}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Rating Summary
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            StarRatingDisplay(rating = averageRating)
                            Text(
                                text = "$averageRating ($totalReviews Ulasan)",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Black, fontWeight = FontWeight.Medium)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = saka.description,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, color = Color.Black)
                        )
                    }

                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 24.dp))

                    // Bagian Ulasan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ulasan Pembeli",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            TextButton(onClick = { showReviewDialog = true }) {
                                Text(if (hasReviewed) "Edit Ulasan" else "Tulis Ulasan", color = BurntOrangeish)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (listReviews.isEmpty()) {
                            Text(
                                text = "Belum ada ulasan. Jadilah yang pertama mereview!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            listReviews.take(5).forEach { review ->
                                ReviewItemCard(review)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }

                    HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)

                    // Produk Serupa
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
                    ) {
                        Text(
                            text = "Produk Serupa",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        when(val relatedState = relatedProductsState) {
                            is UiState.Success -> {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 24.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(relatedState.data) { item ->
                                        SuggestedSakaItem(saka = item, onClick = {
                                            navController.navigate(Screen.Detail.createRoute(item.id))
                                        })
                                    }
                                }
                            }
                            is UiState.Loading -> Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                            else -> {}
                        }
                    }
                }
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = "Gagal memuat: ${state.errorMessage}", color = MaterialTheme.colorScheme.error) }
        }
    }
}

// --- HELPER COMPONENTS ---

@Composable
fun ReviewItemCard(review: ReviewItem) {
    Row(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = review.userPhoto,
            contentDescription = review.userName,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = review.userName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                StarRatingDisplay(rating = review.rating.toDouble(), starSize = 14.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (review.comment.isNotEmpty()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (review.imageUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = review.imageUrl,
                    contentDescription = "Foto Ulasan",
                    modifier = Modifier
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun StarRatingDisplay(rating: Double, starSize: androidx.compose.ui.unit.Dp = 20.dp) {
    Row {
        for (i in 1..5) {
            val icon = when {
                i <= rating -> Icons.Filled.Star
                i - 0.5 <= rating -> Icons.AutoMirrored.Outlined.StarHalf
                else -> Icons.Outlined.Star
            }
            val tint = if (i <= rating || i - 0.5 <= rating) BurntOrangeish else Color.Gray
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(starSize))
        }
    }
}

@Composable
fun StarRatingInput(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
        for (i in 1..5) {
            IconButton(onClick = { onRatingChanged(i) }) {
                val isSelected = i <= rating
                Icon(
                    imageVector = if (isSelected) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "Rate $i",
                    tint = if (isSelected) BurntOrangeish else Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
    isLoading: Boolean,
    initialRating: Int = 0,
    initialComment: String = "",
    isEdit: Boolean = false
) {
    var rating by remember { mutableIntStateOf(initialRating) }
    var comment by remember { mutableStateOf(initialComment) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "Edit Ulasan" else "Tulis Ulasan") },
        text = {
            Column {
                Text("Berikan rating untuk produk ini:", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                StarRatingInput(rating = rating, onRatingChanged = { rating = it })
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Komentar Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (rating > 0) onSubmit(rating, comment) },
                enabled = !isLoading && rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = BurntOrangeish)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                else Text(if (isEdit) "Update" else "Kirim")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}