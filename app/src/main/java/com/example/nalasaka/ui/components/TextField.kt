package com.example.nalasaka.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null,
    // --- PERBAIKAN: Menambahkan parameter placeholder ---
    placeholder: String? = null,
    // --- PARAMETER YANG SUDAH ADA ---
    maxLines: Int = 1 // Default 1 baris
) {
    val isPasswordField = keyboardType == KeyboardType.Password

    var passwordVisible by remember { mutableStateOf(false) }

    // Tentukan VisualTransformation
    val visualTransformation = if (isPasswordField) {
        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        // --- PERBAIKAN: Menggunakan placeholder di OutlinedTextField ---
        placeholder = if (placeholder != null) { { Text(placeholder) } } else null,
        // -----------------------------------------------------------
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        isError = isError,
        singleLine = !isPasswordField && maxLines == 1,
        maxLines = maxLines,
        trailingIcon = {
            if (isPasswordField) {
                val iconImage = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = iconImage, contentDescription = description)
                }
            }
        },
        supportingText = {
            if (isError && errorMessage != null) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }
        },
        modifier = modifier
    )
}