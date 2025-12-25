package com.example.nalasaka.ui.screen.addsaka

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.ui.components.CustomTextField
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.viewmodel.AddSakaViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import com.example.nalasaka.utils.FileUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSakaScreen(
    navController: NavHostController,
    viewModel: AddSakaViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val uploadState by viewModel.uploadState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var discountPriceText by remember { mutableStateOf("") } // NEW: State Diskon
    var stockText by remember { mutableStateOf("") }

    val categories = listOf("Sayur", "Buah", "Rempah", "Beras/Biji-bijian", "Lainnya")
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }

    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val fileProviderAuthority = "${context.packageName}.provider"

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imageError = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraImageUri != null) {
            selectedImageUri = cameraImageUri
            imageError = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, fileProviderAuthority, photoFile)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(uploadState) {
        when (val state = uploadState) {
            is UiState.Success -> {
                showSuccessDialog = true
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(message = "Gagal: ${state.errorMessage}", duration = SnackbarDuration.Long)
                viewModel.resetUploadState()
            }
            else -> {}
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetUploadState()
                navController.popBackStack()
            },
            title = { Text("Berhasil!") },
            text = { Text("Produk berhasil ditambahkan ke toko Anda.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetUploadState()
                        navController.popBackStack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Pilih Sumber Gambar") },
            text = { Text("Ambil foto produk dari kamera atau galeri?") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    imagePickerLauncher.launch("image/*")
                }) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galeri")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, fileProviderAuthority, photoFile)
                        cameraImageUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Kamera")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Produk Baru", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { showImageSourceDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Camera, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Ketuk untuk pilih foto", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            if (imageError != null) {
                Text(text = imageError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            CustomTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = "Nama Produk",
                isError = nameError != null,
                errorMessage = nameError
            )

            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Kategori") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = { selectedCategory = selectionOption; expandedCategory = false }
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(
                    value = priceText,
                    onValueChange = { priceText = it; priceError = null },
                    label = "Harga Normal (Rp)",
                    keyboardType = KeyboardType.Number,
                    isError = priceError != null,
                    errorMessage = priceError,
                    modifier = Modifier.weight(1f)
                )

                CustomTextField(
                    value = stockText,
                    onValueChange = { stockText = it },
                    label = "Stok",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            CustomTextField(
                value = discountPriceText,
                onValueChange = { discountPriceText = it },
                label = "Harga Diskon (Opsional)",
                placeholder = "Kosongkan jika tidak ada promo",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.fillMaxWidth()
            )

            CustomTextField(
                value = description,
                onValueChange = { description = it },
                label = "Deskripsi Produk",
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "UNGGAH PRODUK",
                onClick = {
                    val priceInt = priceText.toIntOrNull() ?: 0
                    val discountInt = discountPriceText.toIntOrNull()
                    val stockInt = stockText.toIntOrNull() ?: 0

                    if (selectedImageUri != null && name.isNotBlank() && priceInt > 0) {
                        val imageFile = FileUtils.uriToFile(selectedImageUri!!, context)
                        viewModel.uploadSaka(
                            imageFile,
                            name,
                            selectedCategory,
                            description,
                            priceInt,
                            discountInt,
                            stockInt
                        )
                    } else {
                        if (selectedImageUri == null) imageError = "Foto wajib ada"
                        if (name.isBlank()) nameError = "Nama wajib diisi"
                        if (priceInt <= 0) priceError = "Harga tidak valid"
                    }
                },
                isLoading = uploadState is UiState.Loading
            )
        }
    }
}