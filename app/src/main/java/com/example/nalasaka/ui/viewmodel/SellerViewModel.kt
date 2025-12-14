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

class SellerViewModel(private val repository: UserRepository) : ViewModel() {

    private val _myProductsState = MutableStateFlow<UiState<List<SakaItem>>>(UiState.Idle)
    val myProductsState: StateFlow<UiState<List<SakaItem>>> = _myProductsState.asStateFlow()

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
}