package com.example.nalasaka.ui.screen.detail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog // Import Dialog
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
import com.example.nalasaka.ui.viewmodel.CartViewModel
import com.example.nalasaka.utils.FileUtils
import java.io.File

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
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context)),
    cartViewModel: CartViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val context = LocalContext.current
    val sakaDetailState by viewModel.sakaDetailState.collectAsState()
    val relatedProductsState by viewModel.relatedProductsState.collectAsState()
    val reviewState by viewModel.reviewState.collectAsState()
    val submitReviewState by viewModel.submitReviewState.collectAsState()
    val addToCartState by cartViewModel.addToCartState.collectAsState()

    // Collect state wishlist
    val isWishlist by viewModel.isWishlist.collectAsState()

    // Ambil data User Login
    val userSession by authViewModel.userSession.collectAsState(initial = com.example.nalasaka.data.pref.UserModel("","","",false))
    val currentUserId = userSession.userId

    var showReviewDialog by remember { mutableStateOf(false) }

    // [BARU] State untuk menampilkan Pop-up Sukses Keranjang
    var showAddToCartSuccessDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    var hasReviewed by remember { mutableStateOf(false) }
    var myReview: ReviewItem? by remember { mutableStateOf(null) }
    var selectedReviewImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedReviewImageUri = uri
        }
    }

    LaunchedEffect(sakaId) {
        viewModel.loadSakaDetail(sakaId)
    }

    LaunchedEffect(submitReviewState) {
        when (val state = submitReviewState) {
            is UiState.Success -> {
                showReviewDialog = false
                selectedReviewImageUri = null
                val msg = if (hasReviewed) "Ulasan diperbarui!" else "Ulasan terkirim!"
                snackbarHostState.showSnackbar(msg)
                viewModel.resetSubmitState()
                viewModel.loadSakaDetail(sakaId)
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(state.errorMessage)
                viewModel.resetSubmitState()
            }
            else -> {}
        }
    }

    // [MODIFIKASI] Effect untuk Add To Cart -> Munculkan Dialog
    LaunchedEffect(addToCartState) {
        when (val state = addToCartState) {
            is UiState.Success -> {
                // Tampilkan Custom Dialog, bukan Snackbar
                showAddToCartSuccessDialog = true
                cartViewModel.resetAddToCartState()
            }
            is UiState.Error -> {
                // Jika error, tetap pakai Snackbar agar tidak mengganggu flow
                snackbarHostState.showSnackbar(state.errorMessage)
                cartViewModel.resetAddToCartState()
            }
            else -> {}
        }
    }

    // [BARU] Render Custom Dialog jika state true
    if (showAddToCartSuccessDialog) {
        AddToCartSuccessDialog(
            onDismiss = { showAddToCartSuccessDialog = false },
            onGoToCart = {
                showAddToCartSuccessDialog = false
                navController.navigate(Screen.Cart.route)
            }
        )
    }

    if (showReviewDialog) {
        AddReviewDialog(
            onDismiss = {
                showReviewDialog = false
                selectedReviewImageUri = null
            },
            onSubmit = { rating, comment, imageUri ->
                val imageFile = imageUri?.let { FileUtils.uriToFile(it, context) }
                viewModel.submitReview(sakaId, rating, comment, imageFile)
            },
            onSelectImage = {
                imagePickerLauncher.launch("image/*")
            },
            isLoading = submitReviewState is UiState.Loading,
            initialRating = myReview?.rating ?: 0,
            initialComment = myReview?.comment ?: "",
            selectedImageUri = selectedReviewImageUri,
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.toggleWishlist(sakaId) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            if (isWishlist) {
                                Icon(Icons.Filled.Favorite, "Hapus dari Wishlist", tint = Color.Red)
                            } else {
                                Icon(Icons.Default.FavoriteBorder, "Tambah ke Wishlist", tint = Color.Black)
                            }
                        }

                        IconButton(
                            onClick = { navController.navigate(Screen.Cart.route) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Lihat Keranjang", tint = Color.Black)
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    PrimaryButton(
                        text = "+ KERANJANG",
                        onClick = {
                            cartViewModel.addToCartFromDetail(sakaId)
                        },
                        isLoading = addToCartState is UiState.Loading,
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

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = saka.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                ),
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            if (saka.isSellerVerified) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Verified Seller",
                                    tint = Color(0xFF07C91F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Text(
                            text = "Kategori: ${saka.category}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

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

// [BARU] Composable Custom Dialog Sukses Masuk Keranjang
@Composable
fun AddToCartSuccessDialog(
    onDismiss: () -> Unit,
    onGoToCart: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Ikon Centang Besar
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50), // Hijau Sukses
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Berhasil Ditambahkan!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Produk telah masuk ke keranjang belanja Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tombol Lanjut Belanja (Outlined/Text)
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Lanjut", color = MaterialTheme.colorScheme.primary)
                    }

                    // Tombol Ke Keranjang (Primary Color)
                    Button(
                        onClick = onGoToCart,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BurntOrangeish)
                    ) {
                        Text("Keranjang")
                    }
                }
            }
        }
    }
}

// ... (Sisa kode ReviewItemCard, StarRatingDisplay, StarRatingInput, AddReviewDialog tetap sama) ...
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
    onSubmit: (Int, String, Uri?) -> Unit,
    onSelectImage: () -> Unit,
    isLoading: Boolean,
    initialRating: Int = 0,
    initialComment: String = "",
    selectedImageUri: Uri? = null,
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

                Spacer(modifier = Modifier.height(16.dp))

                Text("Foto Produk (Opsional):", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                if (selectedImageUri != null) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Preview Foto",
                            modifier = Modifier
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = onSelectImage,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(Color.White.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Ganti")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onSelectImage,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tambah Foto")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (rating > 0) onSubmit(rating, comment, selectedImageUri) },
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