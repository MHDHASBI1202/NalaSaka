package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.data.remote.response.SellerStats
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SellerViewModel(
    private val repository: UserRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    // State untuk Statistik Dashboard
    private val _statsState = MutableStateFlow<UiState<SellerStats>>(UiState.Idle)
    val statsState: StateFlow<UiState<SellerStats>> = _statsState

    // State untuk List Produk Milik Seller
    private val _myProductsState = MutableStateFlow<UiState<List<SakaItem>>>(UiState.Idle)
    val myProductsState: StateFlow<UiState<List<SakaItem>>> = _myProductsState

    // State untuk aksi (Update Stok / Delete)
    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState

    // NEW: State untuk Broadcast Promo
    private val _broadcastState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val broadcastState: StateFlow<UiState<String>> = _broadcastState

    /**
     * Memuat data statistik untuk Dashboard Seller
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _statsState.value = UiState.Loading
            try {
                val user = userPreference.getUser().first()
                val response = repository.getSellerStats(user.token)
                if (!response.error) {
                    _statsState.value = UiState.Success(response.stats)
                } else {
                    _statsState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _statsState.value = UiState.Error(e.message ?: "Terjadi kesalahan koneksi")
            }
        }
    }

    /**
     * Memuat daftar produk yang diunggah oleh seller ini
     */
    fun loadMyProducts() {
        viewModelScope.launch {
            _myProductsState.value = UiState.Loading
            try {
                val user = userPreference.getUser().first()
                val response = repository.getMyProducts(user.token)
                if (!response.error) {
                    _myProductsState.value = UiState.Success(response.listSaka)
                } else {
                    _myProductsState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _myProductsState.value = UiState.Error(e.message ?: "Gagal memuat produk")
            }
        }
    }

    /**
     * Update stok produk tertentu
     */
    fun updateStock(sakaId: String, newStock: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                val user = userPreference.getUser().first()
                val response = repository.updateStock(user.token, sakaId, newStock)
                if (!response.error) {
                    _actionState.value = UiState.Success(response.message)
                    loadMyProducts() // Refresh list setelah update
                } else {
                    _actionState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Gagal update stok")
            }
        }
    }

    /**
     * Hapus produk milik seller
     */
    fun deleteProduct(sakaId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                val user = userPreference.getUser().first()
                val response = repository.deleteSaka(user.token, sakaId)
                if (!response.error) {
                    _actionState.value = UiState.Success(response.message)
                    loadMyProducts() // Refresh list setelah hapus
                } else {
                    _actionState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _actionState.value = UiState.Error(e.message ?: "Gagal hapus produk")
            }
        }
    }

    /**
     * NEW: Mengirim broadcast promo ke semua followers (1x24 Jam)
     */
    fun broadcastPromo() {
        viewModelScope.launch {
            _broadcastState.value = UiState.Loading
            try {
                val user = userPreference.getUser().first()
                val response = repository.broadcastPromo(user.token)
                if (!response.error) {
                    _broadcastState.value = UiState.Success(response.message)
                } else {
                    // API akan mengembalikan error jika belum 24 jam
                    _broadcastState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _broadcastState.value = UiState.Error(e.message ?: "Terjadi kesalahan saat menyiarkan promo")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = UiState.Idle
    }

    fun resetBroadcastState() {
        _broadcastState.value = UiState.Idle
    }
}