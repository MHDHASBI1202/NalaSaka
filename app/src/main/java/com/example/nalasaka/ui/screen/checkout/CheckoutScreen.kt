package com.example.nalasaka.ui.screen.checkout

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.viewmodel.CartViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import com.google.android.gms.location.LocationServices
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavHostController,
    subtotal: Int,
    cartViewModel: CartViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current)),
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val context = LocalContext.current
    val cartState by cartViewModel.cartState.collectAsState()
    val checkoutState by cartViewModel.checkoutState.collectAsState()

    val userModel by authViewModel.userSession.collectAsState(initial = UserModel("","","", false))
    val userToken = userModel.token

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val groupedItems = remember(cartState) {
        if (cartState is UiState.Success) {
            (cartState as UiState.Success).data.groupBy { it.storeName ?: "Toko Tidak Dikenal" }
        } else emptyMap()
    }

    var savedAddress by remember { mutableStateOf("") }
    var showAddressDialog by remember { mutableStateOf(false) }

    var shippingType by remember { mutableStateOf("Diantar") }
    val shippingMethods = listOf(Pair("Reguler", 10000), Pair("Kilat", 20000))
    var expandedCourier by remember { mutableStateOf(false) }
    var selectedCourier by remember { mutableStateOf(shippingMethods[0]) }

    val paymentMethods = listOf("Transfer Bank", "Dana/OVO", "COD")
    var expandedPayment by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf(paymentMethods[0]) }

    LaunchedEffect(Unit) {
        cartViewModel.loadCart()
    }

    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Tentukan Alamat Utama") },
            text = {
                Column {
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                                    loc?.let {
                                        val geocoder = Geocoder(context, Locale.getDefault())
                                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                                        if (!addresses.isNullOrEmpty()) {
                                            savedAddress = addresses[0].getAddressLine(0)
                                            showAddressDialog = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MyLocation, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Gunakan GPS")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Ketik Manual Alamat Utama:", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = savedAddress,
                        onValueChange = { savedAddress = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Ketik alamat lengkap di sini...") }
                    )
                    TextButton(
                        onClick = {
                            if (savedAddress.isNotEmpty()) {
                                val geocoder = Geocoder(context, Locale.getDefault())
                                val results = geocoder.getFromLocationName(savedAddress, 1)
                                if (!results.isNullOrEmpty()) {
                                    savedAddress = results[0].getAddressLine(0)
                                    Toast.makeText(context, "Alamat Utama Tervalidasi!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("Validasi") }
                }
            },
            confirmButton = { Button(onClick = { showAddressDialog = false }) { Text("Simpan") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState())) {

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(selected = shippingType == "Diantar", onClick = { shippingType = "Diantar" }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)) { Text("Diantar") }
                SegmentedButton(selected = shippingType == "Ambil ke Toko", onClick = { shippingType = "Ambil ke Toko" }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)) { Text("Ambil ke Toko") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (shippingType == "Diantar") {
                Text("Alamat Pengiriman", fontWeight = FontWeight.Bold)
                Card(
                    onClick = { showAddressDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.Red)
                        Spacer(Modifier.width(8.dp))
                        Text(if(savedAddress.isEmpty()) "Tambah Alamat Pengiriman (Wajib)" else savedAddress, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Detail Pesanan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            groupedItems.forEach { (storeName, items) ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(storeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        HorizontalDivider(Modifier.padding(vertical = 12.dp))

                        items.forEach { item ->
                            Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = item.photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Medium, maxLines = 1)
                                    Text("${formatRupiah(item.price)} x ${item.quantity}", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }

                        if (shippingType == "Ambil ke Toko") {
                            val storeLat = items.first().latitude
                            val storeLng = items.first().longitude
                            val storeNameItem = items.first().storeName ?: "Toko"

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                onClick = {
                                    if (storeLat != null && storeLng != null) {
                                        val gmmIntentUri = Uri.parse("geo:$storeLat,$storeLng?q=$storeLat,$storeLng($storeNameItem)")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(mapIntent)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text("Lihat Lokasi di Google Maps", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = items.first().storeAddress ?: "Alamat tidak tersedia",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (shippingType == "Diantar") {
                Spacer(modifier = Modifier.height(24.dp))
                Text("Metode Pengiriman", fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(expanded = expandedCourier, onExpandedChange = { expandedCourier = !expandedCourier }) {
                    OutlinedTextField(
                        value = "${selectedCourier.first} (${formatRupiah(selectedCourier.second)})",
                        onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourier) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedCourier, onDismissRequest = { expandedCourier = false }) {
                        shippingMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text("${method.first} (${formatRupiah(method.second)})") },
                                onClick = { selectedCourier = method; expandedCourier = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Metode Pembayaran", fontWeight = FontWeight.Bold)
                ExposedDropdownMenuBox(expanded = expandedPayment, onExpandedChange = { expandedPayment = !expandedPayment }) {
                    OutlinedTextField(
                        value = selectedPayment,
                        onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedPayment) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedPayment, onDismissRequest = { expandedPayment = false }) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = { selectedPayment = method; expandedPayment = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val ongkir = if(shippingType == "Diantar") selectedCourier.second else 0
            val biayaJasa = if(shippingType == "Diantar") 1000 else 0

            val discountAmount = if (userModel.isPromoClaimed) 20000 else 0

            val grandTotal = (subtotal + ongkir + biayaJasa - discountAmount).coerceAtLeast(0)

            CostRow("Total Harga", formatRupiah(subtotal))
            if(shippingType == "Diantar") {
                CostRow("Total Ongkos Kirim", formatRupiah(ongkir))
                CostRow("Biaya Jasa Aplikasi", formatRupiah(biayaJasa))
            }

            if (userModel.isPromoClaimed) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Diskon Promo", style = MaterialTheme.typography.bodyMedium)
                    Text("- ${formatRupiah(discountAmount)}", style = MaterialTheme.typography.bodyMedium, color = Color.Green, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Tagihan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(formatRupiah(grandTotal), color = Color(0xFFE67E22), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(
                text = if(shippingType == "Diantar") "KONFIRMASI PESANAN" else "AMBIL KE TOKO",
                onClick = {
                    val finalAddr = if(shippingType == "Diantar") savedAddress else "Ambil di Toko"

                    if(finalAddr.isEmpty() && shippingType == "Diantar") {
                        Toast.makeText(context, "Alamat belum diisi!", Toast.LENGTH_SHORT).show()
                    } else {
                        if (cartState is UiState.Success) {
                            val items = (cartState as UiState.Success).data

                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                    val userLat = location?.latitude ?: 0.0
                                    val userLng = location?.longitude ?: 0.0

                                    var completedCount = 0
                                    items.forEach { item ->
                                        cartViewModel.processCheckout(
                                            token = userToken,
                                            sakaId = item.sakaId.toInt(),
                                            qty = item.quantity,
                                            method = selectedPayment,
                                            addr = finalAddr,
                                            sub = item.price * item.quantity,
                                            total = grandTotal,
                                            ship = shippingType,
                                            lat = userLat,
                                            lng = userLng,
                                            onSuccess = {
                                                completedCount++
                                                if (completedCount == items.size) {
                                                    if (userModel.isPromoClaimed) {
                                                        authViewModel.markPromoAsUsed()
                                                    }
                                                    Toast.makeText(context, "Pesanan Berhasil!", Toast.LENGTH_LONG).show()
                                                    navController.navigate(Screen.TransactionHistory.route) {
                                                        popUpTo(navController.graph.startDestinationId) {
                                                            saveState = false
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = false
                                                    }
                                                }
                                            }
                                        )
                                    }
                                    cartViewModel.checkoutCart(selectedPayment)
                                }
                            } else {
                                Toast.makeText(context, "Izin lokasi diperlukan untuk checkout!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                isLoading = checkoutState is UiState.Loading
            )
        }
    }
}

@Composable
fun CostRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.DarkGray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}