package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.data.remote.response.TransactionItem
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: UserRepository) : ViewModel() {

    // State untuk List Riwayat
    private val _historyState = MutableStateFlow<UiState<List<TransactionItem>>>(UiState.Loading)
    val historyState: StateFlow<UiState<List<TransactionItem>>> = _historyState

    // State untuk Proses Checkout (Loading/Success/Error)
    private val _checkoutState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val checkoutState: StateFlow<UiState<String>> = _checkoutState

    // Fungsi Ambil Riwayat
    fun getHistory() {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            try {
                // Ambil data user (token & userId) dari DataStore
                val user = repository.getUser().first()
                if (user.token.isNotEmpty()) {
                    val response = repository.getTransactionHistory(user.token, user.userId)
                    if (!response.error) {
                        _historyState.value = UiState.Success(response.history)
                    } else {
                        _historyState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _historyState.value = UiState.Error(e.message ?: "Terjadi Kesalahan")
            }
        }
    }

    fun updateTransactionStatus(transactionId: Int, status: String) {
        viewModelScope.launch {
            try {
                val token = repository.getAuthToken()

                if (token.isNotEmpty()) {
                    val response = repository.updateStatus(token, transactionId, status)
                    if (!response.error) {
                        getHistory()
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    // Fungsi Checkout / Pesan Ulang
    fun checkoutItem(sakaId: String, quantity: Int = 1) { // Parameter sakaId, bukan trxId
        viewModelScope.launch {
            _checkoutState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                val response = repository.checkout(user.token, user.userId, sakaId, quantity, "CASH")
                if (!response.error) {
                    _checkoutState.value = UiState.Success(response.message)
                    // Refresh history setelah beli
                    getHistory()
                } else {
                    _checkoutState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _checkoutState.value = UiState.Error(e.message ?: "Gagal Checkout")
            }
        }
    }

    // Reset state agar snackbar tidak muncul terus
    fun resetCheckoutState() {
        _checkoutState.value = UiState.Loading // Atau state Idle buatan sendiri jika mau
    }
}