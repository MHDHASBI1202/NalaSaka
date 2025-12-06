package com.example.nalasaka.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SellerViewModel(private val repository: UserRepository) : ViewModel() {
    // State untuk input form
    var name by mutableStateOf("")
    var phoneNumber by mutableStateOf("")
    var address by mutableStateOf("")

    // State untuk UI (Menggunakan UiState dari UIState.kt)
    var uiState by mutableStateOf<UiState<String>>(UiState.Idle)
        private set

    fun updateName(newName: String) { name = newName }
    fun updatePhoneNumber(newNumber: String) { phoneNumber = newNumber }
    fun updateAddress(newAddress: String) { address = newAddress }

    fun becomeSeller() {
        if (name.isBlank() || phoneNumber.isBlank() || address.isBlank()) {
            uiState = UiState.Error("Semua kolom harus diisi.")
            return
        }

        uiState = UiState.Loading

        viewModelScope.launch {
            try {
                // 1. Check for login status first
                val user = repository.getUser().first()
                if (!user.isLogin) {
                    uiState = UiState.Error("Anda harus login untuk mendaftar sebagai penjual.")
                    return@launch
                }

                // 2. Panggil repository. (Fungsi repository sekarang mengembalikan ResponseSaka langsung)
                val response = repository.becomeSeller(name, phoneNumber, address)

                // 3. Proses Response
                if (!response.error) {
                    uiState = UiState.Success(response.message)
                } else {
                    // Jika API mengembalikan error: true di dalam respons HTTP 200/201, tangani di sini
                    uiState = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                // Tangkap semua Exception (termasuk kegagalan koneksi/API)
                uiState = UiState.Error(e.message ?: "Gagal menghubungi server.")
            }
        }
    }

    fun resetUiState() {
        uiState = UiState.Idle
    }
}