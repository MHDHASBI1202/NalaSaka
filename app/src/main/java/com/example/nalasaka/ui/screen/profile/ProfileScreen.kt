package com.example.nalasaka.ui.screen.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.ProfileViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import com.example.nalasaka.data.remote.response.ProfileData
import com.example.nalasaka.ui.theme.BurntOrangeish
import com.example.nalasaka.utils.FileUtils
import java.io.File

@Composable
fun getAuthViewModel(navController: NavHostController): AuthViewModel {
    return viewModel(factory = ViewModelFactory.getInstance(navController.context))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController,
    viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory.getInstance(navController.context))
) {
    val profileState by viewModel.profileState.collectAsState()
    val uploadPhotoState by viewModel.uploadPhotoState.collectAsState()
    val authViewModel = getAuthViewModel(navController)
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(uploadPhotoState) {
        when (val state = uploadPhotoState) {
            is UiState.Success -> {
                Toast.makeText(context, state.data, Toast.LENGTH_SHORT).show()
                viewModel.resetUploadPhotoState()
            }
            is UiState.Error -> {
                Toast.makeText(context, state.errorMessage, Toast.LENGTH_SHORT).show()
                viewModel.resetUploadPhotoState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil", color = MaterialTheme.colorScheme.onPrimary) },
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
        when (val state = profileState) {
            is UiState.Idle, UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is UiState.Success -> {
                val profile = state.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFEBEBEB))
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(
                        profile = profile,
                        onUploadPhoto = { file -> viewModel.uploadPhoto(file) },
                        isUploading = uploadPhotoState is UiState.Loading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    if (profile.role == "seller" && profile.storeName != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(
                                    text = "Nama Toko",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.storeName,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (profile.verificationStatus == "verified") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Verified Store",
                                            tint = Color(0xFF07C91F),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    ProfileDetailsSection(profile = profile, navController = navController)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Aktivitas Saya",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column {
                            ProfileMenuItem(
                                icon = Icons.Default.Favorite,
                                title = "Wishlist Saya",
                                onClick = { navController.navigate(Screen.Wishlist.route) }
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                            ProfileMenuItem(
                                icon = Icons.Default.Lock,
                                title = "Ganti Password",
                                onClick = { navController.navigate(Screen.ChangePassword.route) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    PrimaryButton(
                        text = "LOG OUT",
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Welcome.route) { popUpTo(Screen.Home.route) { inclusive = true } }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (profile.role != "seller") {
                        TextButton(onClick = { navController.navigate(Screen.VerifySeller.route) }) {
                            Text(
                                text = "Mulai Menjual",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    } else {
                        when (profile.verificationStatus) {
                            "verified" -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF07C91F))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Akun Penjual Terverifikasi", color = Color(0xFF07C91F), fontWeight = FontWeight.Bold)
                                }
                            }
                            "pending" -> {
                                Text("Menunggu Verifikasi...", color = Color.Gray)
                            }
                            else -> {
                                TextButton(onClick = { navController.navigate(Screen.UploadCertification.route) }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VerifiedUser, null, tint = BurntOrangeish)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Verifikasi Akun (Upload Dokumen)", color = BurntOrangeish, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { Text(text = "Gagal memuat profil: ${state.errorMessage}", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
fun ProfileHeader(
    profile: ProfileData,
    onUploadPhoto: (File) -> Unit,
    isUploading: Boolean
) {
    val context = LocalContext.current
    var showSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onUploadPhoto(FileUtils.uriToFile(it, context)) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            onUploadPhoto(FileUtils.uriToFile(tempCameraUri!!, context))
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("Ubah Foto Profil") },
            text = { Text("Pilih sumber foto, Yang Mulia.") },
            confirmButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("Galeri") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    val photoFile = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.provider", photoFile
                    )
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                }) { Text("Kamera") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(enabled = !isUploading) { showSourceDialog = true }
                    ) {
                        AsyncImage(
                            model = profile.photoUrl,
                            contentDescription = profile.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            error = rememberVectorPainter(Icons.Default.Person)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .background(BurntOrangeish, CircleShape)
                            .padding(4.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.fillMaxSize())
                    }

                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.size(80.dp), color = BurntOrangeish)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (profile.verificationStatus == "verified") {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified User",
                                tint = Color(0xFF07C91F),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FollowStatItem("Pengikut", profile.followersCount.toString())
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.3f)))
                FollowStatItem("Mengikuti", profile.followingCount.toString())
            }
        }
    }
}

@Composable
fun FollowStatItem(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ProfileDetailsSection(profile: ProfileData, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                ProfileDetailItem(label = "Nama", value = profile.name)
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                ProfileDetailItem(label = "Email", value = profile.email)
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                ProfileDetailItem(label = "Nomor HP", value = profile.phoneNumber ?: "Belum ditambahkan")
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                ProfileDetailItem(label = "Alamat", value = profile.address ?: "Belum ditambahkan")
            }
            IconButton(
                onClick = { navController.navigate(Screen.EditProfile.route) },
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Edit Profil",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ProfileDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(16.dp)
        )
    }
}