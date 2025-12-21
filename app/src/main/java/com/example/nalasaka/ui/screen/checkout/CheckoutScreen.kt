import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.nalasaka.ui.components.PrimaryButton
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nalasaka.data.remote.response.CartItem
import com.example.nalasaka.ui.components.formatRupiah
import com.example.nalasaka.ui.viewmodel.CartViewModel
import com.example.nalasaka.ui.viewmodel.UiState
import com.example.nalasaka.ui.viewmodel.ViewModelFactory
import android.location.Geocoder
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavHostController,
    subtotal: Int,
    cartViewModel: CartViewModel = viewModel(factory = ViewModelFactory.getInstance(LocalContext.current))
) {
    val context = LocalContext.current
    val cartState by cartViewModel.cartState.collectAsState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // --- State Alamat ---
    var savedAddress by remember { mutableStateOf("") } // Alamat Utama (Dari GPS/Manual Pop-up)
    var tempAddress by remember { mutableStateOf("") }  // Alamat Sementara (Manual di Layar)
    var showAddressDialog by remember { mutableStateOf(false) }
    var isEditingTempAddress by remember { mutableStateOf(false) }

    // --- State Dropdown & Ringkasan ---
    var shippingType by remember { mutableStateOf("Diantar") }
    val shippingMethods = listOf(Pair("Reguler", 10000), Pair("Kilat", 20000))
    var expandedCourier by remember { mutableStateOf(false) }
    var selectedCourier by remember { mutableStateOf(shippingMethods[0]) }

    val paymentMethods = listOf("Transfer Bank", "Dana/OVO", "COD")
    var expandedPayment by remember { mutableStateOf(false) }
    var selectedPayment by remember { mutableStateOf(paymentMethods[0]) }

    // Ambil data terbaru dari database saat masuk checkout
    LaunchedEffect(Unit) {
        cartViewModel.loadCart()
    }

    // --- DIALOG TENTUKAN LOKASI (KHUSUS ALAMAT UTAMA) ---
    if (showAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddressDialog = false },
            title = { Text("Tentukan Alamat Utama") },
            text = {
                Column {
                    // Opsi A: GPS
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

                    // Opsi B: Manual (Untuk Alamat Utama)
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
            confirmButton = {
                Button(onClick = { showAddressDialog = false }) { Text("Simpan") }
            }
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

            // Tab Pilih Diantar / Ambil
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(selected = shippingType == "Diantar", onClick = { shippingType = "Diantar" }, shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)) { Text("Diantar") }
                SegmentedButton(selected = shippingType == "Ambil ke Toko", onClick = { shippingType = "Ambil ke Toko" }, shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)) { Text("Ambil ke Toko") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (shippingType == "Diantar") {
                // --- BAGIAN ALAMAT ---
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

                // Logika: Alamat Sementara muncul hanya jika Alamat Utama sudah ada
                if (savedAddress.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Kirim ke Alamat Lain? (Opsional)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    if (!isEditingTempAddress && tempAddress.isEmpty()) {
                        OutlinedButton(onClick = { isEditingTempAddress = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.AddCircleOutline, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Tambah Alamat Sementara")
                        }
                    } else {
                        OutlinedTextField(
                            value = tempAddress,
                            onValueChange = { tempAddress = it },
                            label = { Text("Alamat Sementara") },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    val results = geocoder.getFromLocationName(tempAddress, 1)
                                    if (!results.isNullOrEmpty()) {
                                        tempAddress = results[0].getAddressLine(0)
                                        Toast.makeText(context, "Alamat Sementara Valid!", Toast.LENGTH_SHORT).show()
                                    }
                                }) { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32)) }
                            }
                        )
                        Button(onClick = { tempAddress = ""; isEditingTempAddress = false }) {
                            Text("Batal", color = Color.Red)
                        }
                    }
                }
            }

            // --- DETAIL PESANAN VISUAL (DIBAWAH ALAMAT) ---
            Spacer(modifier = Modifier.height(24.dp))
            Text("Detail Pesanan", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            if (cartState is UiState.Success) {
                (cartState as UiState.Success).data.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Gambar Produk
                            AsyncImage(
                                model = item.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(item.name, fontWeight = FontWeight.Medium, maxLines = 1)
                                Row {
                                    Text(formatRupiah(item.price), color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    Text(" x ${item.quantity}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // --- KURIR & PEMBAYARAN ---
            if (shippingType == "Diantar") {
                Spacer(modifier = Modifier.height(16.dp))
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

            // --- RINGKASAN PEMBAYARAN ---
            Spacer(modifier = Modifier.height(24.dp))
            val ongkir = if(shippingType == "Diantar") selectedCourier.second else 0
            CostRow("Total Harga", formatRupiah(subtotal))
            if(shippingType == "Diantar") CostRow("Total Ongkos Kirim", formatRupiah(ongkir))
            CostRow("Biaya Jasa Aplikasi", formatRupiah(1000))
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Tagihan", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(formatRupiah(subtotal + ongkir + 1000), color = Color(0xFFE67E22), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
            PrimaryButton(text = "KONFIRMASI PESANAN", onClick = {
                val finalAddr = if(tempAddress.isNotEmpty()) tempAddress else savedAddress
                if(finalAddr.isEmpty() && shippingType == "Diantar") {
                    Toast.makeText(context, "Alamat belum diisi!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Pesanan dikirim ke: $finalAddr", Toast.LENGTH_LONG).show()
                }
            })
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