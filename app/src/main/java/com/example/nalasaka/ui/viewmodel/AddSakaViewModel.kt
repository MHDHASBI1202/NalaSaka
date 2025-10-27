package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddSakaViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uploadState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uploadState: StateFlow<UiState<String>> = _uploadState.asStateFlow()

    fun uploadSaka(imageFile: File, name: String, description: String, price: Int) {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading
            try {
                // 1. Ambil token pengguna
                val user = repository.getUser().first()
                if (!user.isLogin) {
                    _uploadState.value = UiState.Error("Anda harus login untuk mengunggah produk.")
                    return@launch
                }

                // 2. Siapkan Request Body untuk data multipart (Upload Foto Barang)
                val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                val imageMultipart = MultipartBody.Part.createFormData(
                    "photo", // Nama field API untuk file
                    imageFile.name,
                    requestImageFile
                )

                val nameRequestBody = name.toRequestBody("text/plain".toMediaType())
                val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())
                val priceRequestBody = price.toString().toRequestBody("text/plain".toMediaType())

                // 3. Panggil API
                val response = repository.addNewSaka(
                    user.token,
                    imageMultipart,
                    nameRequestBody,
                    descriptionRequestBody,
                    priceRequestBody
                )

                if (!response.error) {
                    _uploadState.value = UiState.Success(response.message)
                } else {
                    _uploadState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uploadState.value = UiState.Error(e.message ?: "Gagal mengunggah produk.")
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UiState.Idle
    }
}