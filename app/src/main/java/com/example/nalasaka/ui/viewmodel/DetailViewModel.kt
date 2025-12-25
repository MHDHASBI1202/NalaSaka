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

    private val _sakaDetailState = MutableStateFlow<UiState<SakaItem>>(UiState.Idle)
    val sakaDetailState: StateFlow<UiState<SakaItem>> = _sakaDetailState.asStateFlow()

    private val _relatedProductsState = MutableStateFlow<UiState<List<SakaItem>>>(UiState.Idle)
    val relatedProductsState: StateFlow<UiState<List<SakaItem>>> = _relatedProductsState.asStateFlow()

    private val _reviewState = MutableStateFlow<UiState<ReviewData>>(UiState.Idle)
    val reviewState: StateFlow<UiState<ReviewData>> = _reviewState.asStateFlow()

    private val _submitReviewState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val submitReviewState: StateFlow<UiState<String>> = _submitReviewState.asStateFlow()

    private val _isWishlist = MutableStateFlow<Boolean>(false)
    val isWishlist: StateFlow<Boolean> = _isWishlist.asStateFlow()

    fun loadSakaDetail(sakaId: String) {
        viewModelScope.launch {
            _sakaDetailState.value = UiState.Loading
            _relatedProductsState.value = UiState.Loading
            _reviewState.value = UiState.Loading

            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.getDetailSaka(user.token, sakaId)
                    if (!response.error) {
                        _sakaDetailState.value = UiState.Success(response.saka)

                        val allSakaResponse = repository.getAllSaka(user.token)
                        if (!allSakaResponse.error) {
                            val allProducts = allSakaResponse.listSaka
                            val related = allProducts.filter { it.id != sakaId }.shuffled().take(3)
                            _relatedProductsState.value = UiState.Success(related)
                        }

                        loadReviews(user.token, sakaId)

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

    fun checkWishlistStatus(sakaId: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val response = repository.checkWishlist(user.token, sakaId)
                    _isWishlist.value = response.isWishlist
                }
            } catch (e: Exception) {
            }
        }
    }

    fun toggleWishlist(sakaId: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    val oldState = _isWishlist.value
                    _isWishlist.value = !oldState

                    val response = repository.toggleWishlist(user.token, sakaId)

                    if (!response.error) {
                        _isWishlist.value = response.isWishlist
                    } else {
                        _isWishlist.value = oldState
                    }
                }
            } catch (e: Exception) {
                _isWishlist.value = !_isWishlist.value
            }
        }
    }

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    fun checkFollowStatus(sellerId: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUser().first()
                if (user.isLogin && sellerId != user.userId) {
                    val response = repository.checkFollowStatus(user.token, sellerId)
                    _isFollowing.value = response.isFollowing
                }
            } catch (e: Exception) { }
        }
    }

    fun toggleFollow(sellerId: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUser().first()
                if (user.isLogin) {
                    _isFollowing.value = !_isFollowing.value

                    val response = repository.toggleFollow(user.token, sellerId)

                    if (!response.error) {
                        _isFollowing.value = response.isFollowing
                    }
                }
            } catch (e: Exception) {
                _isFollowing.value = !_isFollowing.value
            }
        }
    }
}