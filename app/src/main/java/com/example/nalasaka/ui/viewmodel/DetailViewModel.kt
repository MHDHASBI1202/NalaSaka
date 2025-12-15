package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DetailViewModel(private val repository: UserRepository) : ViewModel() {

    private val _sakaDetailState = MutableStateFlow<UiState<SakaItem>>(UiState.Idle)
    val sakaDetailState: StateFlow<UiState<SakaItem>> = _sakaDetailState.asStateFlow()

    // NEW: State untuk Produk Serupa (Real Data)
    private val _relatedProductsState = MutableStateFlow<UiState<List<SakaItem>>>(UiState.Idle)
    val relatedProductsState: StateFlow<UiState<List<SakaItem>>> = _relatedProductsState.asStateFlow()

    fun loadSakaDetail(sakaId: String) {
        viewModelScope.launch {
            _sakaDetailState.value = UiState.Loading
            _relatedProductsState.value = UiState.Loading // Load related juga
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    // 1. Load Detail
                    val response = repository.getDetailSaka(user.token, sakaId)
                    if (!response.error) {
                        _sakaDetailState.value = UiState.Success(response.saka)

                        // 2. Load Related Products (Ambil semua, lalu filter & shuffle)
                        val allSakaResponse = repository.getAllSaka(user.token)
                        if (!allSakaResponse.error) {
                            val allProducts = allSakaResponse.listSaka
                            // Ambil 3 produk acak selain produk yang sedang dilihat
                            val related = allProducts.filter { it.id != sakaId }.shuffled().take(3)
                            _relatedProductsState.value = UiState.Success(related)
                        }
                    } else {
                        _sakaDetailState.value = UiState.Error(response.message)
                    }
                } else {
                    _sakaDetailState.value = UiState.Error("User not logged in.")
                }
            } catch (e: Exception) {
                _sakaDetailState.value = UiState.Error(e.message ?: "Gagal memuat data.")
            }
        }
    }
}