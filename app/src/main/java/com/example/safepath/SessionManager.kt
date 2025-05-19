package com.example.safepath

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val sharedPref = context.getSharedPreferences("SafePathPrefs", Context.MODE_PRIVATE)

    fun saveLoginSession(user: FirebaseUser) {
        sharedPref.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("userId", user.uid)
            putString("userEmail", user.email)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return sharedPref.getBoolean("isLoggedIn", false) &&
                FirebaseAuth.getInstance().currentUser != null
    }

    fun clearSession() {
        sharedPref.edit { clear() }
    }
}