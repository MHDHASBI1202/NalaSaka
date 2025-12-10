package com.example.nalasaka.data.repository

import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.remote.response.AllSakaResponse
import com.example.nalasaka.data.remote.response.CheckoutResponse
import com.example.nalasaka.data.remote.response.DetailSakaResponse
import com.example.nalasaka.data.remote.response.LoginResult
import com.example.nalasaka.data.remote.response.ResponseSaka
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.data.remote.response.ProfileResponse // Import baru
import com.example.nalasaka.data.remote.response.ProfileData
import com.example.nalasaka.data.remote.response.TransactionHistoryResponse
import com.example.nalasaka.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UserRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    // --- DataStore Operations ---
    fun getUser(): Flow<UserModel> {
        return userPreference.getUser()
    }

    suspend fun saveUser(user: UserModel) {
        userPreference.saveUser(user)
    }

    suspend fun logout() {
        userPreference.logout()
    }


    // --- Remote API Operations ---
    suspend fun register(
        name: String,
        email: String,
        password: String,
        // --- PERUBAHAN BARU: Tambah no_hp dan alamat ---
        phoneNumber: String,
        address: String,
        passwordConfirmation: String // Ditambah untuk 'confirmed' di backend
        // -----------------------------------------------
    ): ResponseSaka {
        // Logika mocking bisa ditambahkan di sini, tapi umumnya Register tetap ke API
        return apiService.register(name, email, phoneNumber, address, password, passwordConfirmation)
    }

    suspend fun login(email: String, password: String): ResponseSaka {
        val DUMMY_EMAIL = "test@nalasaka.com"
        val DUMMY_TOKEN = "MOCK_TOKEN_FOR_TESTING" // Token yang akan digunakan untuk mocking data

        if (email == DUMMY_EMAIL) {
            // MOCKING MODE: Simulasi Login Sukses
            val mockLoginResult = LoginResult("user-mock-123", "User Tester", DUMMY_TOKEN)

            saveUser(
                UserModel(mockLoginResult.userId, mockLoginResult.name, mockLoginResult.token, true)
            )

            return ResponseSaka(
                error = false,
                message = "Login Sukses (MOCKING MODE)",
                loginResult = mockLoginResult
            )
        }

        // JIKA BUKAN MOCK EMAIL: Lanjutkan ke API Call sebenarnya
        val response = apiService.login(email, password)
        if (!response.error && response.loginResult != null) {
            saveUser(
                UserModel(
                    response.loginResult.userId,
                    response.loginResult.name,
                    response.loginResult.token,
                    true
                )
            )
        }
        return response
    }

    suspend fun getAllSaka(token: String): AllSakaResponse {
        if (token == "MOCK_TOKEN_FOR_TESTING") {
            // MOCKING MODE: Simulasi Daftar Produk untuk Home Screen
            val dummyList = listOf(
                SakaItem(
                    id = "saka-001",
                    name = "Tomat Segar Organik",
                    description = "Hasil panen terbaik dari petani lokal.",
                    photoUrl = "https://i.imgur.com/8Qh1c4Y.png", // Ganti dengan URL gambar placeholder
                    price = 15000
                ),
                SakaItem(
                    id = "saka-002",
                    name = "Bawang Merah Lokal",
                    description = "Bawang merah kualitas super, tersedia dalam 1kg.",
                    photoUrl = "https://i.imgur.com/K1S2Y9C.png",
                    price = 45000
                )
            )
            return AllSakaResponse(error = false, message = "Data sukses dimuat (MOCKING)", listSaka = dummyList)
        }

        // API Call sebenarnya
        return apiService.getAllSaka("Bearer $token")
    }

    suspend fun getDetailSaka(token: String, sakaId: String): DetailSakaResponse {
        if (token == "MOCK_TOKEN_FOR_TESTING") {
            // MOCKING MODE: Simulasi Detail Produk (PENTING untuk modul Anda)
            val mockSakaDetail = SakaItem(
                id = sakaId,
                name = "Tomat Segar Organik",
                description = "Deskripsi produk tomat yang sangat panjang. Ini adalah area tempat modul Reputasi & Analisis (Rating & Ulasan) akan diintegrasikan.",
                photoUrl = "https://i.imgur.com/8Qh1c4Y.png",
                price = 15000
            )
            return DetailSakaResponse(error = false, message = "Detail sukses dimuat (MOCKING)", saka = mockSakaDetail)
        }
        return apiService.getDetailSaka("Bearer $token", sakaId)
    }

    suspend fun addNewSaka(
        token: String,
        file: MultipartBody.Part,
        name: RequestBody,
        description: RequestBody,
        price: RequestBody
    ): ResponseSaka {
        if (token == "MOCK_TOKEN_FOR_TESTING") {
            return ResponseSaka(error = false, message = "Produk berhasil diunggah (MOCKING)")
        }
        return apiService.addNewSaka("Bearer $token", file, name, description, price)
    }

    // --- MODUL TRANSAKSI ---

    suspend fun getTransactionHistory(token: String, userId: String): TransactionHistoryResponse {
        return apiService.getTransactionHistory("Bearer $token", userId)
    }

    suspend fun checkout(token: String, userId: String, sakaId: String, quantity: Int): CheckoutResponse {
        return apiService.checkoutTransaction("Bearer $token", userId, sakaId, quantity)
    }


    // --- TAMBAHAN UNTUK MODUL PROFIL ---
    suspend fun getUserProfile(token: String): ProfileResponse {
        val DUMMY_EMAIL = "test@nalasaka.com" // Digunakan untuk mock

        if (token == "MOCK_TOKEN_FOR_TESTING") {
            // MOCKING MODE: Simulasi Detail Profil Pengguna
            val mockProfile = ProfileData(
                userId = "user-mock-123",
                name = "User Tester NalaSaka",
                email = DUMMY_EMAIL,
                photoUrl = "https://i.imgur.com/K1S2Y9C.png", // URL Gambar Profil Placeholder
                phoneNumber = "081234567890",
                address = "Jl. Pertanian Jaya No. 42, Kota Sejahtera"
            )
            return ProfileResponse(
                error = false,
                message = "Detail Profil sukses dimuat (MOCKING)",
                user = mockProfile
            )
        }

        // API Call sebenarnya
        return apiService.getUserProfile("Bearer $token")
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(apiService, userPreference).also { instance = it }
            }
    }
}