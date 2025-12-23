package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.remote.response.OrderItem
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.data.remote.response.SellerStats
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SellerViewModel(private val repository: UserRepository, userPreference: UserPreference) : ViewModel() {

    private val _myProductsState = MutableStateFlow<UiState<List<SakaItem>>>(UiState.Idle)
    val myProductsState = _myProductsState.asStateFlow()

    private val _statsState = MutableStateFlow<UiState<SellerStats>>(UiState.Idle)
    val statsState = _statsState.asStateFlow()

    private val _ordersState = MutableStateFlow<UiState<List<OrderItem>>>(UiState.Idle)
    val ordersState = _ordersState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState = _actionState.asStateFlow()

    private suspend fun getToken(): String {
        return "Bearer ${repository.getUser().first().token}"
    }

    fun loadMyProducts() {
        viewModelScope.launch {
            _myProductsState.value = UiState.Loading
            try {
                val response = repository.getMyProducts(getToken())
                _myProductsState.value = UiState.Success(response.listSaka)
            } catch (e: Exception) {
                _myProductsState.value = UiState.Error(e.message ?: "Gagal memuat produk")
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _statsState.value = UiState.Loading
            try {
                val response = repository.getSellerStats(getToken())
                _statsState.value = UiState.Success(response.stats)
            } catch (e: Exception) {
                _statsState.value = UiState.Error(e.message ?: "Gagal memuat statistik")
            }
        }
    }

    fun loadSellerOrders() {
        viewModelScope.launch {
            _ordersState.value = UiState.Loading
            try {
                val orders = repository.getSellerOrders(getToken())
                _ordersState.value = UiState.Success(orders)
            } catch (e: Exception) {
                _ordersState.value = UiState.Error(e.message ?: "Gagal memuat pesanan")
            }
        }
    }

    fun updateStatus(orderId: Int, status: String) { // Ubah nama fungsi agar sesuai dengan UI
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repository.updateOrderStatus(getToken(), orderId, status)
                _actionState.value = UiState.Success("Status berhasil diupdate")
                loadSellerOrders()
            } catch (e: Exception) {
                _actionState.value = UiState.Error("Gagal update status")
            }
        }
    }

    fun updateStoreLocation(address: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                repository.updateStoreLocation(getToken(), address, lat, lng)
                _actionState.value = UiState.Success("Lokasi toko diperbarui")
            } catch (e: Exception) {
                _actionState.value = UiState.Error("Gagal update lokasi")
            }
        }
    }

    fun updateStock(sakaId: String, newStock: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repository.updateStock(getToken(), sakaId, newStock)
                _actionState.value = UiState.Success("Stok diperbarui")
                loadMyProducts()
            } catch (e: Exception) {
                _actionState.value = UiState.Error("Gagal update stok")
            }
        }
    }

    fun deleteProduct(sakaId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                repository.deleteSaka(getToken(), sakaId)
                _actionState.value = UiState.Success("Produk dihapus")
                loadMyProducts()
            } catch (e: Exception) {
                _actionState.value = UiState.Error("Gagal menghapus produk")
            }
        }
    }

    fun broadcastPromo() {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                // Memanggil fungsi broadcast di repository yang sudah terhubung ke ApiService
                repository.broadcastPromo(getToken())
                _actionState.value = UiState.Success("Promo berhasil disiarkan ke pengikut, Yang Mulia!")
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Gagal menyiarkan promo")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = UiState.Idle
    }
}