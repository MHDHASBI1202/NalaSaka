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
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AddSakaViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uploadState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uploadState: StateFlow<UiState<String>> = _uploadState.asStateFlow()


    fun uploadSaka(
        imageFile: File,
        name: String,
        category: String,
        description: String,
        price: Int,
        discountPrice: Int?,
        stock: Int
    ) {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (!user.isLogin) {
                    _uploadState.value = UiState.Error("Sesi berakhir, silakan login kembali.")
                    return@launch
                }

                val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                val imageMultipart = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)

                val nameReq = name.toRequestBody("text/plain".toMediaType())
                val catReq = category.toRequestBody("text/plain".toMediaType())
                val descReq = description.toRequestBody("text/plain".toMediaType())
                val priceReq = price.toString().toRequestBody("text/plain".toMediaType())
                val stockReq = stock.toString().toRequestBody("text/plain".toMediaType())

                val discountReq = discountPrice?.toString()?.toRequestBody("text/plain".toMediaType())

                val response = repository.addNewSaka(
                    token = user.token,
                    file = imageMultipart,
                    name = nameReq,
                    category = catReq,
                    description = descReq,
                    price = priceReq,
                    discountPrice = discountReq,
                    stock = stockReq
                )

                if (!response.error) {
                    _uploadState.value = UiState.Success(response.message)
                } else {
                    _uploadState.value = UiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uploadState.value = UiState.Error(e.message ?: "Terjadi kesalahan sistem saat mengunggah.")
            }
        }
    }

    fun resetUploadState() {
        _uploadState.value = UiState.Idle
    }
}