package com.example.nalasaka.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore Delegate untuk akses di seluruh aplikasi
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {

    // Kunci untuk DataStore
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val NAME_KEY = stringPreferencesKey("name")
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val IS_LOGIN_KEY = booleanPreferencesKey("is_login")
    private val ROLE_KEY = stringPreferencesKey("role") // NEW: Kunci untuk Role

    // Mendapatkan model user sebagai Flow
    fun getUser(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            UserModel(
                preferences[USER_ID_KEY] ?: "",
                preferences[NAME_KEY] ?: "",
                preferences[TOKEN_KEY] ?: "",
                preferences[IS_LOGIN_KEY] ?: false,
                preferences[ROLE_KEY] ?: "customer" // NEW: Ambil nilai role, default 'customer'
            )
        }
    }

    // Menyimpan sesi User (Login)
    suspend fun saveUser(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.userId
            preferences[NAME_KEY] = user.name
            preferences[TOKEN_KEY] = user.token
            preferences[IS_LOGIN_KEY] = true // Set isLogin ke true
            preferences[ROLE_KEY] = user.role // NEW: Simpan role
        }
    }

    // Logout / Menghapus sesi
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = ""
            preferences[NAME_KEY] = ""
            preferences[TOKEN_KEY] = ""
            preferences[IS_LOGIN_KEY] = false // Set isLogin ke false
            preferences[ROLE_KEY] = "customer" // NEW: Reset role ke default
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}