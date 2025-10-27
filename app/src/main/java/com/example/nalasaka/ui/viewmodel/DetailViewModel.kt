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

    fun loadSakaDetail(sakaId: String) {
        viewModelScope.launch {
            _sakaDetailState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.getDetailSaka(user.token, sakaId)
                    if (!response.error) {
                        _sakaDetailState.value = UiState.Success(response.saka)
                    } else {
                        _sakaDetailState.value = UiState.Error(response.message)
                    }
                } else {
                    _sakaDetailState.value = UiState.Error("User not logged in.")
                }
            } catch (e: Exception) {
                _sakaDetailState.value = UiState.Error(e.message ?: "Gagal memuat detail produk.")
            }
        }
    }
}