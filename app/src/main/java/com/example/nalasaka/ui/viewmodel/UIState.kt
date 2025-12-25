package com.example.nalasaka.ui.viewmodel

sealed class UiState<out T> {
    val message: String = ""

    object Loading : UiState<Nothing>()
    object Idle : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val errorMessage: String) : UiState<Nothing>()
}