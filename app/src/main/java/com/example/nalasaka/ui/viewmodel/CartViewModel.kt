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

    val totalPrice = MutableStateFlow(0)

    private suspend fun getToken(): String {
        return repository.getUser().first().token
    }

    fun loadCart() {
        viewModelScope.launch {
            _cartState.value = UiState.Loading
            try {
                val token = getToken()
                if (token.isNotEmpty()) {
                    val response = repository.getCart(token)
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
                val token = getToken()
                repository.updateCartQty(token, item.cartId, newQty)
                loadCart()
            } catch (e: Exception) { }
        }
    }

    fun checkoutCart(paymentMethod: String) {
        viewModelScope.launch {
            _checkoutState.value = UiState.Loading
            try {
                val token = getToken()
                val response = repository.checkoutCart(token, paymentMethod)

                if (!response.error) {
                    _checkoutState.value = UiState.Success("Pesanan berhasil dibuat, Yang Mulia!")
                    loadCart()
                } else {
                    _checkoutState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _checkoutState.value = UiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    fun addToCartFromDetail(sakaId: String) {
        viewModelScope.launch {
            _addToCartState.value = UiState.Loading
            try {
                val token = getToken()
                if(token.isNotEmpty()) {
                    val response = repository.addToCart(token, sakaId, 1)
                    if (!response.error) {
                        _addToCartState.value = UiState.Success("Berhasil ditambahkan ke keranjang")
                    } else {
                        _addToCartState.value = UiState.Error(response.message)
                    }
                } else {
                    _addToCartState.value = UiState.Error("Silakan login terlebih dahulu")
                }
            } catch (e: Exception) {
                _addToCartState.value = UiState.Error(e.message ?: "Gagal menambahkan")
            }
        }
    }

    fun resetAddToCartState() {
        _addToCartState.value = UiState.Idle
    }

    fun getUser() = repository.getUser()

    fun processCheckout(
        token: String,
        sakaId: Int,
        qty: Int,
        method: String,
        addr: String,
        sub: Int,
        total: Int,
        ship: String,
        lat: Double?,
        lng: Double?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.createTransaction(token, sakaId, qty, method, addr, sub, total, ship, lat, lng)
                if (!response.error) {
                    onSuccess()
                }
            } catch (e: Exception) {
            }
        }
    }
    fun resetCheckoutState() { _checkoutState.value = UiState.Idle }
    }