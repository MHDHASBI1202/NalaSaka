package com.example.nalasaka.utils

import android.content.Context
import android.widget.Toast

object Utils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Placeholder untuk Validasi Data (misalnya Validasi NIK, Sertifikasi Petani)
    fun isValidCertificationId(id: String): Boolean {
        // Implementasi logika validasi sertifikasi petani di sini
        return id.length == 10 // Contoh validasi placeholder
    }

    // Utilitas lain yang umum dapat ditambahkan di sini.
}