package com.example.biometriclogin

import android.content.Context
import android.util.Patterns
import androidx.appcompat.app.AlertDialog

object EmailValidator {

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}