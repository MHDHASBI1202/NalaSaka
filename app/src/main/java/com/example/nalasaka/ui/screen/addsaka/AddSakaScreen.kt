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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.text.style.TextAlign
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

    // State Input
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stockText by remember { mutableStateOf("") }

    // State Kategori
    val categories = listOf("Sayur", "Buah", "Rempah", "Beras/Biji-bijian", "Lainnya")
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // Errors
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var stockError by remember { mutableStateOf<String?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }

    // Dialog Gambar
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // State Dialog Sukses Upload
    var showSuccessDialog by remember { mutableStateOf(false) }

    val fileProviderAuthority = "${context.packageName}.provider"

    // Launchers
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

    // Effect untuk memantau status upload
    LaunchedEffect(uploadState) {
        when (val state = uploadState) {
            is UiState.Success -> {
                // Munculkan Dialog Sukses alih-alih langsung popBackStack
                showSuccessDialog = true
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(message = "Gagal: ${state.errorMessage}", duration = SnackbarDuration.Long)
                viewModel.resetUploadState()
            }
            else -> {}
        }
    }

    // --- DIALOG SUKSES UPLOAD ---
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                // Jika user klik di luar dialog, tetap tutup dan kembali
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
                        navController.popBackStack() // Kembali ke dashboard
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("OK")
                }
            }
        )
    }

    // --- DIALOG PILIHAN GAMBAR ---
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Pilih Sumber Gambar") },
            text = { Text("Ambil foto produk dari kamera atau galeri?") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    imagePickerLauncher.launch("image/*") // Buka Galeri
                }) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Galeri")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    // Cek Izin Kamera
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
            // Area Foto (Klik untuk buka Dialog)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { showImageSourceDialog = true }, // Klik memicu Dialog Gambar
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
                            Text("Ketuk untuk ambil foto/pilih dari galeri", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            if (imageError != null) {
                Text(text = imageError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Input Nama Produk
            CustomTextField(
                value = name,
                onValueChange = { name = it; nameError = null },
                label = "Nama Produk",
                isError = nameError != null,
                errorMessage = nameError
            )

            // Dropdown Kategori
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

            // Row untuk Harga dan Stok bersebelahan
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CustomTextField(
                    value = priceText,
                    onValueChange = { priceText = it; priceError = null },
                    label = "Harga (Rp)",
                    keyboardType = KeyboardType.Number,
                    isError = priceError != null,
                    errorMessage = priceError,
                    modifier = Modifier.weight(1f)
                )

                // Input Stok
                CustomTextField(
                    value = stockText,
                    onValueChange = { stockText = it; stockError = null },
                    label = "Stok",
                    keyboardType = KeyboardType.Number,
                    isError = stockError != null,
                    errorMessage = stockError,
                    modifier = Modifier.weight(1f)
                )
            }

            // Input Deskripsi
            CustomTextField(
                value = description,
                onValueChange = { description = it; descriptionError = null },
                label = "Deskripsi Produk",
                maxLines = 5,
                isError = descriptionError != null,
                errorMessage = descriptionError
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Unggah
            PrimaryButton(
                text = "UNGGAH PRODUK",
                onClick = {
                    if (validateUploadInput(selectedImageUri, name, description, priceText, stockText,
                            { nameError = it }, { descriptionError = it }, { priceError = it }, { stockError = it }, { imageError = it })) {

                        val priceInt = priceText.toIntOrNull() ?: 0
                        val stockInt = stockText.toIntOrNull() ?: 0
                        val imageFile = FileUtils.uriToFile(selectedImageUri!!, context)

                        viewModel.uploadSaka(imageFile, name, selectedCategory, description, priceInt, stockInt)
                    }
                },
                isLoading = uploadState is UiState.Loading
            )
        }
    }
}

// Fungsi Validasi Input Upload
private fun validateUploadInput(
    imageUri: Uri?,
    name: String,
    description: String,
    priceText: String,
    stockText: String,
    onNameError: (String) -> Unit,
    onDescriptionError: (String) -> Unit,
    onPriceError: (String) -> Unit,
    onStockError: (String) -> Unit,
    onImageError: (String) -> Unit
): Boolean {
    var isValid = true
    if (imageUri == null) { onImageError("Foto wajib dipilih"); isValid = false }
    if (name.isBlank()) { onNameError("Nama produk wajib diisi"); isValid = false }
    if (description.isBlank()) { onDescriptionError("Deskripsi wajib diisi"); isValid = false }

    val priceInt = priceText.toIntOrNull()
    if (priceText.isBlank() || priceInt == null || priceInt <= 0) {
        onPriceError("Harga wajib diisi angka > 0"); isValid = false
    }

    // Validasi Stok
    val stockInt = stockText.toIntOrNull()
    if (stockText.isBlank() || stockInt == null || stockInt < 0) {
        onStockError("Stok wajib diisi angka >= 0"); isValid = false
    }

    return isValid
}