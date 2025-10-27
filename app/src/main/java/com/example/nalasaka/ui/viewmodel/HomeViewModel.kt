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

class HomeViewModel(private val repository: UserRepository) : ViewModel() {

    private val _sakaState = MutableStateFlow<UiState<List<SakaItem>>>(UiState.Idle)
    val sakaState: StateFlow<UiState<List<SakaItem>>> = _sakaState.asStateFlow()

    init {
        // Cek sesi dan muat data saat pertama kali ViewModel dibuat
        checkSessionAndLoadSaka()
    }

    private fun checkSessionAndLoadSaka() {
        viewModelScope.launch {
            val user = repository.getUser().first()
            if (user.isLogin) {
                loadSaka(user.token)
            } else {
                // Jika belum login, biarkan state Idle atau berikan notifikasi
                _sakaState.value = UiState.Error("User not logged in. Redirecting...")
            }
        }
    }

    fun loadSaka(token: String) {
        viewModelScope.launch {
            _sakaState.value = UiState.Loading
            try {
                val response = repository.getAllSaka(token)
                if (!response.error) {
                    _sakaState.value = UiState.Success(response.listSaka)
                } else {
                    _sakaState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _sakaState.value = UiState.Error(e.message ?: "Gagal memuat daftar produk.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}