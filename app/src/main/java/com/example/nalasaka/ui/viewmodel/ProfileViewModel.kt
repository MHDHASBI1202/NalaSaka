package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.remote.response.ProfileData
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<ProfileData>>(UiState.Idle)
    val profileState: StateFlow<UiState<ProfileData>> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<ProfileData>>(UiState.Idle)
    val updateState: StateFlow<UiState<ProfileData>> = _updateState.asStateFlow()

    // NEW: State untuk memantau proses aktivasi seller
    private val _sellerActivationState = MutableStateFlow<UiState<ProfileData>>(UiState.Idle)
    val sellerActivationState: StateFlow<UiState<ProfileData>> = _sellerActivationState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.getUserProfile(user.token)
                    if (!response.error) {
                        _profileState.value = UiState.Success(response.user)
                    } else {
                        _profileState.value = UiState.Error(response.message)
                    }
                } else {
                    _profileState.value = UiState.Error("User not logged in. Redirecting...")
                }
            } catch (e: Exception) {
                _profileState.value = UiState.Error(e.message ?: "Gagal memuat detail profil.")
            }
        }
    }

    fun updateProfile(name: String, phoneNumber: String, address: String, storeName: String? = null) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.updateUserProfile(user.token, name, phoneNumber, address, storeName)
                    if (!response.error) {
                        _updateState.value = UiState.Success(response.user)
                        _profileState.value = UiState.Success(response.user)

                        if (user.name != response.user.name) {
                            repository.saveUser(user.copy(name = response.user.name))
                        }
                    } else {
                        _updateState.value = UiState.Error(response.message)
                    }
                } else {
                    _updateState.value = UiState.Error("User not logged in.")
                }
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Gagal memperbarui profil.")
            }
        }
    }

    // NEW: Fungsi untuk mengaktifkan mode penjual
    fun activateSellerMode(storeName: String) {
        viewModelScope.launch {
            _sellerActivationState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.activateSellerMode(user.token, storeName)
                    if (!response.error) {
                        _sellerActivationState.value = UiState.Success(response.user)
                        _profileState.value = UiState.Success(response.user)
                    } else {
                        _sellerActivationState.value = UiState.Error(response.message)
                    }
                } else {
                    _sellerActivationState.value = UiState.Error("User not logged in.")
                }
            } catch (e: Exception) {
                _sellerActivationState.value = UiState.Error(e.message ?: "Gagal mengaktifkan mode penjual.")
            }
        }
    }


    fun resetUpdateState() {
        _updateState.value = UiState.Idle
    }

    // NEW: Fungsi untuk mereset seller activation state
    fun resetSellerActivationState() {
        _sellerActivationState.value = UiState.Idle
    }
}