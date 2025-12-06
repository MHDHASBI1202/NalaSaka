package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.remote.response.ResponseSaka
import com.example.nalasaka.data.repository.UserRepository
import com.example.nalasaka.data.pref.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    // --- PERBAIKAN: Ekspos sesi pengguna secara publik ---
    val userSession: Flow<UserModel> = repository.getUser()
    // ----------------------------------------------------

    private val _loginState = MutableStateFlow<UiState<ResponseSaka>>(UiState.Idle)
    val loginState: StateFlow<UiState<ResponseSaka>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<ResponseSaka>>(UiState.Idle)
    val registerState: StateFlow<UiState<ResponseSaka>> = _registerState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            try {
                val response = repository.login(email, password)
                if (!response.error) {
                    _loginState.value = UiState.Success(response)
                } else {
                    _loginState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "Terjadi kesalahan saat login")
            }
        }
    }

    // Fungsi Register yang diperbarui dengan semua parameter baru
    fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String, // Parameter baru
        address: String,     // Parameter baru
        isSeller: Boolean    // Parameter baru
    ) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            try {
                // Memanggil fungsi repository dengan semua parameter baru
                val response = repository.register(name, email, password, phoneNumber, address, isSeller)

                if (!response.error) {
                    _registerState.value = UiState.Success(response)
                } else {
                    _registerState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.message ?: "Terjadi kesalahan saat registrasi")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}