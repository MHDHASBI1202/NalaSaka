package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.data.remote.response.ProfileData
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileViewModel(private val repository: UserRepository) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<ProfileData>>(UiState.Idle)
    val profileState: StateFlow<UiState<ProfileData>> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<ProfileData>>(UiState.Idle)
    val updateState: StateFlow<UiState<ProfileData>> = _updateState.asStateFlow()

    // State untuk memantau proses aktivasi seller
    private val _sellerActivationState = MutableStateFlow<UiState<ProfileData>>(UiState.Idle)
    val sellerActivationState: StateFlow<UiState<ProfileData>> = _sellerActivationState.asStateFlow()

    // State untuk Upload Sertifikasi
    private val _uploadCertState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uploadCertState: StateFlow<UiState<String>> = _uploadCertState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            // Jangan set Loading jika data sudah ada agar tidak flickering saat kembali
            if (_profileState.value is UiState.Idle || _profileState.value is UiState.Error) {
                _profileState.value = UiState.Loading
            }

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
                    _profileState.value = UiState.Error("User not logged in.")
                }
            } catch (e: Exception) {
                _profileState.value = UiState.Error(e.message ?: "Gagal memuat profil.")
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
                        // Update nama di lokal agar sinkron
                        repository.saveUser(user.copy(name = response.user.name))
                    } else {
                        _updateState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Gagal update profil.")
            }
        }
    }

    fun activateSellerMode(storeName: String) {
        viewModelScope.launch {
            _sellerActivationState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.activateSellerMode(user.token, storeName)

                    if (!response.error) {
                        _sellerActivationState.value = UiState.Success(response.user)

                        val updatedUser = UserModel(
                            userId = response.user.userId,
                            name = response.user.name,
                            token = user.token,
                            isLogin = true,
                            role = "seller"
                        )
                        repository.saveUser(updatedUser)

                        _profileState.value = UiState.Success(response.user)
                    } else {
                        _sellerActivationState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _sellerActivationState.value = UiState.Error(e.message ?: "Gagal aktivasi seller.")
            }
        }
    }

    // [FUNGSI YANG DIPERBAIKI]
    fun uploadCertification(file: File) {
        viewModelScope.launch {
            _uploadCertState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val requestImageFile = file.asRequestBody("image/jpeg".toMediaType())
                    val imageMultipart = MultipartBody.Part.createFormData("photo", file.name, requestImageFile)

                    val response = repository.uploadCertification(user.token, imageMultipart)

                    if (!response.error) {
                        _uploadCertState.value = UiState.Success("Verifikasi berhasil diajukan!")

                        // [CRITICAL FIX] Update profileState secara manual dengan data terbaru dari API Upload
                        // Ini yang membuat halaman Profil langsung berubah tanpa perlu loading ulang
                        _profileState.value = UiState.Success(response.user)

                    } else {
                        _uploadCertState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _uploadCertState.value = UiState.Error(e.message ?: "Gagal upload dokumen")
            }
        }
    }

    fun resetUpdateState() {
        _updateState.value = UiState.Idle
    }

    fun resetSellerActivationState() {
        _sellerActivationState.value = UiState.Idle
    }

    fun resetUploadCertState() {
        _uploadCertState.value = UiState.Idle
    }

    private val _changePassState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val changePassState: StateFlow<UiState<String>> = _changePassState.asStateFlow()

    fun changePassword(currentPass: String, newPass: String, newPassConfirm: String) {
        viewModelScope.launch {
            _changePassState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.changePassword(user.token, currentPass, newPass, newPassConfirm)
                    if (!response.error) {
                        _changePassState.value = UiState.Success(response.message)
                    } else {
                        _changePassState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _changePassState.value = UiState.Error(e.message ?: "Gagal ganti password")
            }
        }
    }

    fun resetChangePassState() {
        _changePassState.value = UiState.Idle
    }

    private val _uploadPhotoState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uploadPhotoState: StateFlow<UiState<String>> = _uploadPhotoState.asStateFlow()

    fun uploadPhoto(file: java.io.File) {
        viewModelScope.launch {
            _uploadPhotoState.value = UiState.Loading
            try {
                val user = repository.getUser().first()

                // Konversi file ke MultipartBody
                val requestImageFile = okhttp3.RequestBody.create("image/jpeg".toMediaTypeOrNull(), file)
                val imageMultipart = MultipartBody.Part.createFormData("photo", file.name, requestImageFile)

                val response = repository.updateProfilePhoto(user.token, imageMultipart)
                if (!response.error) {
                    _uploadPhotoState.value = UiState.Success("Foto profil berhasil diperbarui!")
                    loadUserProfile() // Refresh data profil agar foto langsung berubah
                } else {
                    _uploadPhotoState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uploadPhotoState.value = UiState.Error(e.message ?: "Terjadi kesalahan")
            }
        }
    }

    // Tambahkan fungsi ini untuk menghilangkan error resetUploadPhotoState
    fun resetUploadPhotoState() {
        _uploadPhotoState.value = UiState.Idle
    }
}