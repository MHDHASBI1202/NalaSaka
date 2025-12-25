package com.example.nalasaka.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.nalasaka.ui.navigation.Screen

sealed class BottomBarItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomBarItem(Screen.Home.route, Icons.Default.Home, "Home")
    object Produk : BottomBarItem(Screen.Produk.route, Icons.Default.Inventory2, "Produk")

    object Toko : BottomBarItem(Screen.SellerDashboard.route, Icons.Default.Store, "Toko")

    object Pesanan : BottomBarItem(Screen.TransactionHistory.route, Icons.Default.ReceiptLong, "Pesanan")
    object Profil : BottomBarItem(Screen.Profile.route, Icons.Default.Person, "Profil")
}

val items = listOf(
    BottomBarItem.Home,
    BottomBarItem.Produk,
    BottomBarItem.Toko,
    BottomBarItem.Pesanan,
    BottomBarItem.Profil,
)

@Composable
fun SakaBottomBar(
    navController: NavHostController,
    userRole: String
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    var showNotSellerDialog by remember { mutableStateOf(false) }

    if (showNotSellerDialog) {
        AlertDialog(
            onDismissRequest = { showNotSellerDialog = false },
            title = { Text("Akses Ditolak") },
            text = { Text("Anda belum terdaftar sebagai Penjual. Silakan aktifkan mode penjual di menu Profil.") },
            confirmButton = {
                Button(
                    onClick = {
                        showNotSellerDialog = false
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Ke Profil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotSellerDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    NavigationBar(
        modifier = Modifier,
        containerColor = Color.White,
        tonalElevation = 5.dp
    ) {
        items.forEach { item ->
            AddItem(
                item = item,
                navController = navController,
                currentRoute = currentRoute,
                userRole = userRole,
                onShowDialog = { showNotSellerDialog = true }
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    item: BottomBarItem,
    navController: NavHostController,
    currentRoute: String?,
    userRole: String,
    onShowDialog: () -> Unit
) {
    val isSelected = currentRoute?.startsWith(item.route) == true
    val selectedColor = MaterialTheme.colorScheme.secondary
    val unselectedColor = Color.Gray

    NavigationBarItem(
        icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
        label = { Text(item.label, maxLines = 1) },
        selected = isSelected,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = selectedColor,
            selectedTextColor = selectedColor,
            unselectedIconColor = unselectedColor,
            unselectedTextColor = unselectedColor,
            indicatorColor = Color.White
        ),
        onClick = {
            if (item == BottomBarItem.Toko) {
                if (userRole == "seller") {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                } else {
                    onShowDialog()
                }
            } else {
                if (currentRoute != item.route) {
                    navController.navigate(item.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    )
}