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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
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
import com.example.nalasaka.di.Injection
import com.example.nalasaka.ui.components.MyTextField // DIGANTI: Menggunakan MyTextField yang benar
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
) {
    val context = LocalContext.current
    // FIX: Gunakan LocalContext untuk Injection dan ViewModelFactory
    val repository = Injection.provideRepository(context)
    val viewModel: AddSakaViewModel = viewModel(factory = ViewModelFactory(repository))

    val uploadState by viewModel.uploadState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // --- Logika Penjual ---
    val userPref = repository.getUser()
    val userModel by userPref.collectAsState(initial = null)
    val isSeller = userModel?.isSeller ?: false
    // ----------------------

    // State untuk input form
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }

    // State untuk Error
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var imageError by remember { mutableStateOf<String?>(null) }
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Constants
    val fileProviderAuthority = "${context.packageName}.provider"

    // Launchers
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        imageError = if (uri == null) "Foto wajib dipilih" else null
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
        } else {
            // Memberi tahu user jika izin ditolak
            // Anda bisa menggunakan snackbar di sini jika diperlukan
        }
    }

    // Effect untuk menampilkan hasil upload dan navigasi
    LaunchedEffect(uploadState) {
        when (val state = uploadState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar(state.data.toString(), duration = SnackbarDuration.Long) // Menggunakan data (message)
                viewModel.resetUploadState()
                navController.popBackStack() // Kembali ke Home
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(message = state.errorMessage, duration = SnackbarDuration.Long)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tambahkan Produk (Saka)", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary), // Burnt Orangeish
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        // Akses hanya diizinkan jika pengguna adalah penjual
        if (isSeller) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Area Pemilihan Foto (Gallery)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val imageUri = selectedImageUri
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Selected image",
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Camera, contentDescription = "Select image", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "Ketuk untuk pilih Foto Barang dari Galeri", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                if (imageError != null) {
                    Text(text = imageError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                // Tombol Ambil Foto (Kamera)
                PrimaryButton(
                    text = "AMBIL FOTO DARI KAMERA",
                    onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            val photoFile = File(context.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                            val uri = FileProvider.getUriForFile(context, fileProviderAuthority, photoFile)
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary) // Deep Moss
                )

                // Input Nama Produk
                MyTextField( // DIGANTI dari CustomTextField
                    value = name,
                    onValueChange = { newValue -> name = newValue; nameError = null }, // Eksplisit newValue
                    label = "Nama Produk",
                    isError = nameError != null,
                    errorMessage = nameError
                )

                // Input Harga
                MyTextField( // DIGANTI dari CustomTextField
                    value = priceText,
                    onValueChange = { newValue -> priceText = newValue; priceError = null }, // Eksplisit newValue
                    label = "Harga (Rp)",
                    // Menggunakan KeyboardOptions yang benar
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = priceError != null,
                    errorMessage = priceError
                )

                // Input Deskripsi Produk
                MyTextField( // DIGANTI dari CustomTextField
                    value = description,
                    onValueChange = { newValue -> description = newValue; descriptionError = null }, // Eksplisit newValue
                    label = "Deskripsi Produk",
                    // Mengasumsikan MyTextField mendukung maxLines
                    // Jika tidak, harus diganti dengan komponen yang mendukung banyak baris (misalnya OutlinedTextField biasa)
                    // maxLines = 5,
                    isError = descriptionError != null,
                    errorMessage = descriptionError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tombol Unggah
                PrimaryButton(
                    text = "UNGGAH PRODUK",
                    onClick = {
                        if (validateUploadInput(selectedImageUri, name, description, priceText, { nameError = it }, { descriptionError = it }, { priceError = it }, { imageError = it }) && selectedImageUri != null) {
                            val priceInt = priceText.toIntOrNull()
                            if (priceInt != null) {
                                val imageFile = FileUtils.uriToFile(selectedImageUri!!, context)
                                viewModel.uploadSaka(imageFile, name, description, priceInt)
                            }
                        }
                    },
                    isLoading = uploadState is UiState.Loading
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            // Tampilkan pesan jika pengguna bukan penjual
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Anda harus menjadi penjual terlebih dahulu untuk mengunggah produk. Silakan verifikasi status penjual Anda di halaman Profil.",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Fungsi Validasi Input Upload (Dipertahankan)
private fun validateUploadInput(
    imageUri: Uri?,
    name: String,
    description: String,
    priceText: String,
    onNameError: (String) -> Unit,
    onDescriptionError: (String) -> Unit,
    onPriceError: (String) -> Unit,
    onImageError: (String) -> Unit
): Boolean {
    var isValid = true
    if (imageUri == null) { onImageError("Foto wajib dipilih"); isValid = false }
    if (name.isBlank()) { onNameError("Nama produk wajib diisi"); isValid = false }
    if (description.isBlank()) { onDescriptionError("Deskripsi wajib diisi"); isValid = false }

    val priceInt = priceText.toIntOrNull()
    if (priceText.isBlank() || priceInt == null || priceInt <= 0) { onPriceError("Harga wajib diisi dengan angka positif"); isValid = false }
    return isValid
}