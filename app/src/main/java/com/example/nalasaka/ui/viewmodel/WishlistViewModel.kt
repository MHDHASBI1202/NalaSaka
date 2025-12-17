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

class WishlistViewModel(private val repository: UserRepository) : ViewModel() {

    private val _wishlistState = MutableStateFlow<UiState<List<SakaItem>>>(UiState.Idle)
    val wishlistState: StateFlow<UiState<List<SakaItem>>> = _wishlistState.asStateFlow()

    fun loadWishlist() {
        viewModelScope.launch {
            _wishlistState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.getMyWishlist(user.token)
                    if (!response.error) {
                        _wishlistState.value = UiState.Success(response.listSaka)
                    } else {
                        _wishlistState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _wishlistState.value = UiState.Error(e.message ?: "Gagal memuat wishlist")
            }
        }
    }
}