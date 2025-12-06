package com.example.nalasaka.data.repository

import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.data.pref.UserPreference
import com.example.nalasaka.data.remote.response.AllSakaResponse
import com.example.nalasaka.data.remote.response.BecomeSellerResponse // Import Baru
import com.example.nalasaka.data.remote.response.CheckoutResponse
import com.example.nalasaka.data.remote.response.DetailSakaResponse
import com.example.nalasaka.data.remote.response.LoginResult
import com.example.nalasaka.data.remote.response.ProfileResponse
import com.example.nalasaka.data.remote.response.ProfileData
import com.example.nalasaka.data.remote.response.ResponseSaka
import com.example.nalasaka.data.remote.response.SakaItem
import com.example.nalasaka.data.remote.response.TransactionHistoryResponse
import com.example.nalasaka.data.remote.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    // Fungsi Register yang diperbarui
    suspend fun register(
        name: String,
        email: String,
        password: String,
        phoneNumber: String, // Baru
        address: String,     // Baru
        isSeller: Boolean    // Baru
    ): ResponseSaka {
        // Asumsi: Register tidak menggunakan mocking, langsung ke API
        val response = apiService.register(name, email, password, phoneNumber, address, isSeller)

        // Simpan data jika registrasi sukses dan langsung login
        if (!response.error && response.loginResult != null) {
            saveUser(
                UserModel(
                    response.loginResult.userId,       // FIX: Ambil userId dari LoginResult
                    response.loginResult.name,         // FIX: Ambil name dari LoginResult
                    response.loginResult.token,
                    true,                              // isLogin
                    response.loginResult.isSeller      // isSeller
                )
            )
        }
        return response
    }

    suspend fun login(email: String, password: String): ResponseSaka {
        val DUMMY_EMAIL = "test@nalasaka.com"
        val DUMMY_TOKEN = "MOCK_TOKEN_FOR-TESTING" // Token yang akan digunakan untuk mocking data

        if (email == DUMMY_EMAIL) {
            // MOCKING MODE: Simulasi Login Sukses
            val mockLoginResult = LoginResult(
                name = "User Tester",
                userId = "user-mock-123",
                token = DUMMY_TOKEN,
                isSeller = false // Asumsi mock user awal adalah pembeli
            )

            saveUser(
                // FIX: Memastikan 5 parameter dikirim dalam urutan yang benar
                UserModel(
                    mockLoginResult.userId,
                    mockLoginResult.name,
                    mockLoginResult.token,
                    true, // isLogin
                    mockLoginResult.isSeller // isSeller
                )
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
                // FIX: Memastikan 5 parameter dikirim dalam urutan yang benar
                UserModel(
                    response.loginResult.userId,       // userId
                    response.loginResult.name,         // name
                    response.loginResult.token,        // token
                    true,                              // isLogin
                    response.loginResult.isSeller      // isSeller
                )
            )
        }
        return response
    }

    suspend fun getAllSaka(token: String): AllSakaResponse {
        if (token == "MOCK_TOKEN_FOR-TESTING") {
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
        if (token == "MOCK_TOKEN_FOR-TESTING") {
            // MOCKING MODE: Simulasi Detail Produk
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
        if (token == "MOCK_TOKEN_FOR-TESTING") {
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

        if (token == "MOCK_TOKEN_FOR-TESTING") {
            // MOCKING MODE: Simulasi Detail Profil Pengguna
            val mockProfile = ProfileData(
                userId = "user-mock-123",
                name = "User Tester NalaSaka",
                email = DUMMY_EMAIL,
                photoUrl = "https://i.imgur.com/K1S2Y9C.png", // URL Gambar Profil Placeholder
                phoneNumber = "081234567890",
                address = "Jl. Pertanian Jaya No. 42, Kota Sejahtera",
                isSeller = false // Tambahkan status isSeller untuk mock
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

    // Fungsi Baru: Menjadi Penjual
    suspend fun becomeSeller(name: String, phoneNumber: String, address: String): BecomeSellerResponse {
        val token = userPreference.getUser().first().token
        val DUMMY_EMAIL = "test@nalasaka.com" // Digunakan untuk mock

        if (token == "MOCK_TOKEN_FOR-TESTING") {
            // MOCKING MODE: Simulasi menjadi penjual sukses
            val mockUser = ProfileData(
                userId = "user-mock-123",
                name = name,
                email = DUMMY_EMAIL,
                photoUrl = null,
                phoneNumber = phoneNumber,
                address = address,
                isSeller = true // Penting: status berubah menjadi true
            )

            // Update local pref (status isSeller)
            val currentUser = userPreference.getUser().first()
            userPreference.saveUser(currentUser.copy(isSeller = true))

            return BecomeSellerResponse(
                error = false,
                message = "Berhasil menjadi penjual (MOCKING)",
                user = mockUser
            )
        }

        // API Call sebenarnya
        val response = apiService.becomeSeller(name, phoneNumber, address)
        if (!response.error && response.user != null) {
            // Update status seller di local preferences
            val currentUser = userPreference.getUser().first()
            val updatedUser = currentUser.copy(isSeller = response.user.isSeller)
            userPreference.saveUser(updatedUser)
        }
        return response
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