package com.example.nalasaka.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.theme.DeepMoss
import com.example.nalasaka.ui.theme.LightBackground


// Helper Composable untuk menampilkan setiap baris detail
@Composable
fun AccountDetailItem(label: String, value: String, onEditClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick) // Seluruh baris clickable
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // KIRI: Label (Diberi lebar tetap agar rapi)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
            modifier = Modifier.width(120.dp) // Fixed width for labels
        )

        Spacer(modifier = Modifier.width(8.dp))

        // TENGAH: Value (mengambil sisa ruang)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.weight(1f) // Takes remaining space
        )

        // KANAN: Edit Icon (Menambahkan ikon Edit untuk kerapian dan fungsi)
        IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit $label", tint = DeepMoss)
        }
    }
    Divider(color = Color.LightGray.copy(alpha = 0.5f))
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController
) {
    // Data Dummy untuk Detail Akun (sesuai permintaan)
    val accountDetails = mapOf(
        "Nama Pengguna" to "Budi Fasolasi",
        "Tanggal Lahir" to "15/08/1990",
        "Email" to "budi.f@example.com",
        "No. HP" to "0812-XXXX-XXXX",
        "Status" to "Penjual/Petani Aktif"
    )

    val backgroundColor = LightBackground

    Scaffold(
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- HEADER PROFIL (Deep Moss Section) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepMoss)
                    .padding(vertical = 24.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Placeholder Foto Profil Besar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Foto Profil", modifier = Modifier.size(64.dp), tint = DeepMoss)
                    }

                    Spacer(Modifier.width(16.dp))

                    // Nama Profil
                    Text(
                        text = "Budi Fasolasi",
                        style = MaterialTheme.typography.headlineMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Stats Row (0 Mengikuti, 100 Produk, 1rb Pengikut)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("0 Mengikuti", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                    Text("100 Produk", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                    Text("1rb Pengikut", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                }
            }

            // --- DETAIL AKUN DALAM CARD ---
            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Loop untuk menampilkan semua detail akun yang diminta
                    accountDetails.forEach { (label, value) ->
                        AccountDetailItem(label = label, value = value, onEditClick = { /* TODO: Logic untuk membuka dialog/layar edit */ })
                    }
                }
            }

            // --- ACTION BUTTONS ---
            Spacer(Modifier.height(32.dp))

            // 1. Tombol Log Out (Deep Moss)
            PrimaryButton(
                text = "Log Out",
                onClick = { /* Logic Logout */ },
                modifier = Modifier.padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepMoss),
            )

            Spacer(Modifier.height(16.dp))

            // 2. Link Mulai Menjual
            TextButton(onClick = { /* Navigasi ke Mulai Menjual / Onboarding */ }) {
                Text(
                    text = "Mulai Menjual",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.primary, // Warna Primary (Burnt Orangeish)
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(100.dp)) // Padding untuk Bottom Nav
        }
    }
}