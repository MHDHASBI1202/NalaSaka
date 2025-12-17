package com.example.nalasaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nalasaka.data.remote.response.ReviewData
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.data.remote.response.WishlistResponse
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

class DetailViewModel(private val repository: UserRepository) : ViewModel() {

    // State untuk Detail Produk
    private val _sakaDetailState = MutableStateFlow<UiState<SakaItem>>(UiState.Idle)
    val sakaDetailState: StateFlow<UiState<SakaItem>> = _sakaDetailState.asStateFlow()

    // State untuk Produk Terkait
    private val _relatedProductsState = MutableStateFlow<UiState<List<SakaItem>>>(UiState.Idle)
    val relatedProductsState: StateFlow<UiState<List<SakaItem>>> = _relatedProductsState.asStateFlow()

    // State untuk Data Review (List, Rating Rata2, Total)
    private val _reviewState = MutableStateFlow<UiState<ReviewData>>(UiState.Idle)
    val reviewState: StateFlow<UiState<ReviewData>> = _reviewState.asStateFlow()

    // State untuk proses pengiriman Review (Success/Error msg)
    private val _submitReviewState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val submitReviewState: StateFlow<UiState<String>> = _submitReviewState.asStateFlow()

    // [BARU] State Status Wishlist (Boolean: true jika disukai, false jika tidak)
    private val _isWishlist = MutableStateFlow<Boolean>(false)
    val isWishlist: StateFlow<Boolean> = _isWishlist.asStateFlow()

    // Fungsi Memuat Detail Produk Lengkap
    fun loadSakaDetail(sakaId: String) {
        viewModelScope.launch {
            _sakaDetailState.value = UiState.Loading
            _relatedProductsState.value = UiState.Loading
            _reviewState.value = UiState.Loading

            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    // 1. Load Detail Produk
                    val response = repository.getDetailSaka(user.token, sakaId)
                    if (!response.error) {
                        _sakaDetailState.value = UiState.Success(response.saka)

                        // 2. Load Related Products
                        val allSakaResponse = repository.getAllSaka(user.token)
                        if (!allSakaResponse.error) {
                            val allProducts = allSakaResponse.listSaka
                            val related = allProducts.filter { it.id != sakaId }.shuffled().take(3)
                            _relatedProductsState.value = UiState.Success(related)
                        }

                        // 3. Load Reviews
                        loadReviews(user.token, sakaId)

                        // 4. [AUTO CHECK] Cek status wishlist juga saat load detail
                        checkWishlistStatus(sakaId)

                    } else {
                        _sakaDetailState.value = UiState.Error(response.message)
                    }
                } else {
                    _sakaDetailState.value = UiState.Error("User not logged in.")
                }
            } catch (e: Exception) {
                _sakaDetailState.value = UiState.Error(e.message ?: "Gagal memuat data.")
            }
        }
    }

    // Fungsi helper privat refresh review
    private suspend fun loadReviews(token: String, sakaId: String) {
        try {
            val response = repository.getProductReviews(token, sakaId)
            if (!response.error) {
                _reviewState.value = UiState.Success(response.data)
            } else {
                _reviewState.value = UiState.Error(response.message)
            }
        } catch (e: Exception) {
            _reviewState.value = UiState.Error("Gagal memuat ulasan")
        }
    }

    // Fungsi Kirim Review
    fun submitReview(sakaId: String, rating: Int, comment: String, imageFile: File?) {
        viewModelScope.launch {
            _submitReviewState.value = UiState.Loading
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val sakaIdReq = sakaId.toRequestBody("text/plain".toMediaType())
                    val ratingReq = rating.toString().toRequestBody("text/plain".toMediaType())
                    val commentReq = comment.toRequestBody("text/plain".toMediaType())

                    var imageMultipart: MultipartBody.Part? = null
                    if (imageFile != null) {
                        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
                        imageMultipart = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)
                    }

                    val response = repository.addReview(user.token, sakaIdReq, ratingReq, commentReq, imageMultipart)

                    if (!response.error) {
                        _submitReviewState.value = UiState.Success("Ulasan terkirim!")
                        // Reload data review agar list terupdate
                        loadReviews(user.token, sakaId)
                    } else {
                        _submitReviewState.value = UiState.Error(response.message)
                    }
                }
            } catch (e: Exception) {
                _submitReviewState.value = UiState.Error(e.message ?: "Gagal mengirim ulasan")
            }
        }
    }

    fun resetSubmitState() {
        _submitReviewState.value = UiState.Idle
    }

    // --- FITUR WISHLIST ---

    // Fungsi Cek Status Wishlist Awal (Dipanggil di UI LaunchedEffect)
    fun checkWishlistStatus(sakaId: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.checkWishlist(user.token, sakaId)
                    _isWishlist.value = response.isWishlist
                }
            } catch (e: Exception) {
                // Ignore error check, default false is fine
            }
        }
    }

    // Fungsi Toggle (Klik Tombol Love)
    fun toggleWishlist(sakaId: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    // Optimistic UI Update: Ubah icon langsung sebelum request selesai agar responsif
                    val oldState = _isWishlist.value
                    _isWishlist.value = !oldState

                    val response = repository.toggleWishlist(user.token, sakaId)

                    // Sinkronisasi data server (memastikan status benar sesuai respon backend)
                    if (!response.error) {
                        _isWishlist.value = response.isWishlist
                    } else {
                        // Jika server error, kembalikan ke state awal
                        _isWishlist.value = oldState
                    }
                }
            } catch (e: Exception) {
                // Jika koneksi gagal, kembalikan ke state semula (karena optimistic update)
                _isWishlist.value = !_isWishlist.value
            }
        }
    }
}