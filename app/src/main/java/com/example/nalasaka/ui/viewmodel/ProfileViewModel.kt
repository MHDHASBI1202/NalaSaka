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
}