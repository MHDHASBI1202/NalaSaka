package com.example.nalasaka.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nalasaka.data.repository.UserRepository
import com.example.nalasaka.di.Injection

class ViewModelFactory(private val repository: UserRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            // 1. Authentication ViewModels (Login/Register)
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository) as T
            }
            // 2. Home ViewModel (Modul Produk & Pemasaran)
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            // 3. Detail Product ViewModel
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(repository) as T
            }
            // 4. Add Saka (Upload Foto Barang) ViewModel
            modelClass.isAssignableFrom(AddSakaViewModel::class.java) -> {
                AddSakaViewModel(repository) as T
            }
            // 5. Transaction ViewModel (Modul Transaksi & Logistik)
            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                TransactionViewModel(repository) as T
            }

            // Tambahkan entri untuk SellerViewModel di sini:
            modelClass.isAssignableFrom(SellerViewModel::class.java) -> {
                SellerViewModel(repository) as T
            }

            // --- TAMBAHAN UNTUK MODUL PROFIL ---
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }
            // Tambahan: ViewModel untuk fitur spesifik seperti Wishlist, Rating, dll.
            // ...

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            // Membuat instance UserRepository hanya sekali
            return INSTANCE ?: synchronized(ViewModelFactory::class.java) {
                INSTANCE ?: ViewModelFactory(Injection.provideRepository(context)).also { INSTANCE = it }
            }
        }
    }
}