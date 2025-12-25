package com.example.nalasaka.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.pref.dataStore
import com.example.nalasaka.data.repository.UserRepository
import com.example.nalasaka.di.Injection

class ViewModelFactory(
    private val repository: UserRepository,
    private val userPreference: UserPreference
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddSakaViewModel::class.java) -> {
                AddSakaViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CartViewModel::class.java) -> {
                CartViewModel(repository) as T
            }
            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                TransactionViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SellerViewModel::class.java) -> {
                SellerViewModel(repository, userPreference) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }
            modelClass.isAssignableFrom(WishlistViewModel::class.java) -> {
                WishlistViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SellerOrderViewModel::class.java) -> {
                SellerOrderViewModel(repository) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            return INSTANCE ?: synchronized(ViewModelFactory::class.java) {
                val repository = Injection.provideRepository(context)
                val userPreference = UserPreference.getInstance(context.dataStore)

                INSTANCE ?: ViewModelFactory(repository, userPreference).also { INSTANCE = it }
            }
        }
    }
}