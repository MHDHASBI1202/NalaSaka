package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.remote.response.OrderItem
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SellerOrderViewModel(private val repository: UserRepository) : ViewModel() {

    private val _ordersState = MutableStateFlow<UiState<List<OrderItem>>>(UiState.Loading)
    val ordersState: StateFlow<UiState<List<OrderItem>>> = _ordersState

    fun getSellerOrders() {
        viewModelScope.launch {
            _ordersState.value = UiState.Loading
            try {
                val token = repository.getAuthToken()
                val response = repository.getSellerOrders(token)
                _ordersState.value = UiState.Success(response)
            } catch (e: Exception) {
                _ordersState.value = UiState.Error(e.message ?: "Gagal mengambil pesanan")
            }
        }
    }

    fun updateStatus(orderId: Int, status: String) {
        viewModelScope.launch {
            try {
                val token = repository.getAuthToken()
                val response = repository.updateOrderStatus(token, orderId, status)
                if (!response.error) {
                    getSellerOrders()
                }
            } catch (e: Exception) {
            }
        }
    }
}