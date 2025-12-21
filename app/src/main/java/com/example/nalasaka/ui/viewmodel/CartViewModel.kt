package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.remote.response.CartItem
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CartViewModel(private val repository: UserRepository) : ViewModel() {

    private val _cartState = MutableStateFlow<UiState<List<CartItem>>>(UiState.Idle)
    val cartState: StateFlow<UiState<List<CartItem>>> = _cartState

    private val _checkoutState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val checkoutState: StateFlow<UiState<String>> = _checkoutState

    private val _addToCartState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val addToCartState: StateFlow<UiState<String>> = _addToCartState

    // Hitung total harga lokal
    val totalPrice = MutableStateFlow(0)

    val paymentMethod = MutableStateFlow("CASH")

    fun loadCart() {
        viewModelScope.launch {
            _cartState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.getCart(user.token)
                    if (!response.error) {
                        _cartState.value = UiState.Success(response.data)
                        calculateTotal(response.data)
                    } else {
                        _cartState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _cartState.value = UiState.Error(e.message ?: "Gagal memuat keranjang")
            }
        }
    }

    private fun calculateTotal(items: List<CartItem>) {
        totalPrice.value = items.sumOf { it.price * it.quantity }
    }

    fun updateQuantity(item: CartItem, newQty: Int) {
        viewModelScope.launch {
            try {
                val user = repository.getUser().first()
                repository.updateCartQty(user.token, item.cartId, newQty)
                loadCart() // Refresh data
            } catch (e: Exception) {
                // Handle error silent
            }
        }
    }

    fun checkout() {
        viewModelScope.launch {
            _checkoutState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                val response = repository.checkoutCart(user.token, paymentMethod.value)
                if (!response.error) {
                    _checkoutState.value = UiState.Success(response.message)
                    loadCart() // Keranjang jadi kosong
                } else {
                    _checkoutState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _checkoutState.value = UiState.Error(e.message ?: "Checkout gagal")
            }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = UiState.Idle
    }

    // Fungsi Add to Cart untuk dipanggil dari Detail Screen
    fun addToCartFromDetail(sakaId: String) {
        viewModelScope.launch {
            _addToCartState.value = UiState.Loading // Set status loading
            try {
                val user = repository.getUser().first()
                if(user.isLogin) {
                    val response = repository.addToCart(user.token, sakaId, 1)
                    if (!response.error) {
                        _addToCartState.value = UiState.Success("Berhasil ditambahkan ke keranjang")
                    } else {
                        _addToCartState.value = UiState.Error(response.message)
                    }
                } else {
                    _addToCartState.value = UiState.Error("Silakan login terlebih dahulu")
                }
            } catch (e: Exception) {
                _addToCartState.value = UiState.Error(e.message ?: "Gagal menambahkan ke keranjang")
            }
        }
    }

    // [BARU] Reset state agar snackbar tidak muncul terus menerus saat rotasi layar
    fun resetAddToCartState() {
        _addToCartState.value = UiState.Idle
    }
}