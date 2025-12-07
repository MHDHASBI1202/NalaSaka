package com.example.nalasaka.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.nalasaka.ui.navigation.Screen

sealed class BottomBarItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomBarItem(Screen.Home.route, Icons.Default.Home, "Home")
    object Produk : BottomBarItem("produk_nav", Icons.Default.Inventory2, "Produk")
    object Promo : BottomBarItem("promo_nav", Icons.Default.Discount, "Promo")
    object Pesanan : BottomBarItem("transaction_history", Icons.Default.ReceiptLong, "Pesanan")
    object Profil : BottomBarItem(Screen.Profile.route, Icons.Default.Person, "Profil")
}

val items = listOf(
    BottomBarItem.Home,
    BottomBarItem.Produk,
    BottomBarItem.Promo,
    BottomBarItem.Pesanan,
    BottomBarItem.Profil,
)

@Composable
fun SakaBottomBar(navController: NavHostController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    // Gunakan NavigationBar dari Material3
    NavigationBar(
        modifier = Modifier,
        containerColor = Color.White, // Bottom bar berwarna Putih
        tonalElevation = 5.dp
    ) {
        items.forEach { item ->
            AddItem(
                item = item,
                navController = navController,
                currentRoute = currentRoute
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    item: BottomBarItem,
    navController: NavHostController,
    currentRoute: String?
) {
    val isSelected = currentRoute?.startsWith(item.route) == true

    // Warna aktif menggunakan warna sekunder Yang Mulia (DeepMoss)
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
            indicatorColor = Color.White // Indikator harus hilang atau transparan
        ),
        onClick = {
            if (currentRoute != item.route) {
                navController.navigate(item.route) {
                    // Hindari membangun tumpukan tujuan yang besar di back stack
                    popUpTo(Screen.Home.route) { saveState = true }
                    // Hindari salinan tujuan yang sama saat memilih ulang item yang sama
                    launchSingleTop = true
                    // Kembalikan status saat memilih ulang item yang sebelumnya dipilih
                    restoreState = true
                }
            }
        }
    )
}