package com.example.checkpill.auth

import android.content.Context

class AuthSessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)

    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "").orEmpty()

    fun login(userName: String, pin: String) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_PIN, pin)
            .apply()
    }

    fun logout() {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .apply()
    }

    companion object {
        private const val PREF_NAME = "auth_session"
        private const val KEY_LOGGED_IN = "logged_in"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_PIN = "pin"
    }
}
