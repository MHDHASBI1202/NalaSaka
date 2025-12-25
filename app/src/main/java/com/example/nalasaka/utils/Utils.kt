package com.example.nalasaka.utils

import android.content.Context
import android.widget.Toast

object Utils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun isValidCertificationId(id: String): Boolean {
        return id.length == 10
    }
}