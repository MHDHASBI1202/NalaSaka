package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.data.remote.response.SellerStats
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SellerViewModel(private val repository: UserRepository) : ViewModel() {

    // State untuk List Produk Toko Sendiri (Inventory)
    private val _myProductsState = MutableStateFlow<UiState<List<SakaItem>>>(UiState.Idle)
    val myProductsState: StateFlow<UiState<List<SakaItem>>> = _myProductsState.asStateFlow()

    // State untuk Statistik Penjualan (Dashboard)
    private val _statsState = MutableStateFlow<UiState<SellerStats>>(UiState.Idle)
    val statsState: StateFlow<UiState<SellerStats>> = _statsState.asStateFlow()

    // State untuk Aksi (Update/Delete)
    private val _actionState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val actionState: StateFlow<UiState<String>> = _actionState.asStateFlow()

    // Fungsi memuat stok produk toko sendiri
    fun loadMyProducts() {
        viewModelScope.launch {
            _myProductsState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.getMyProducts(user.token)
                    if (!response.error) {
                        _myProductsState.value = UiState.Success(response.listSaka)
                    } else {
                        _myProductsState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _myProductsState.value = UiState.Error(e.message ?: "Gagal memuat stok.")
            }
        }
    }

    // Fungsi memuat data statistik dashboard
    fun loadDashboardData() {
        viewModelScope.launch {
            _statsState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.getSellerStats(user.token)
                    if (!response.error) {
                        _statsState.value = UiState.Success(response.stats)
                    } else {
                        _statsState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _statsState.value = UiState.Error(e.message ?: "Gagal memuat statistik.")
            }
        }
    }
    // NEW: Update Stok
    fun updateStock(sakaId: String, newStock: Int) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                val response = repository.updateStock(user.token, sakaId, newStock)
                if (!response.error) {
                    _actionState.value = UiState.Success("Stok berhasil diperbarui")
                    loadMyProducts() // Refresh list
                } else {
                    _actionState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _actionState.value = UiState.Error("Gagal update stok")
            }
        }
    }

    // NEW: Hapus Barang
    fun deleteProduct(sakaId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                val response = repository.deleteSaka(user.token, sakaId)
                if (!response.error) {
                    _actionState.value = UiState.Success("Produk dihapus")
                    loadMyProducts() // Refresh list
                } else {
                    _actionState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _actionState.value = UiState.Error("Gagal menghapus produk")
            }
        }
    }

    fun resetActionState() {
        _actionState.value = UiState.Idle
    }
}