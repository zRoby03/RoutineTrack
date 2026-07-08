package com.example.routinetrack.data.session

import android.content.Context

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("routine_track_session", Context.MODE_PRIVATE)

    fun saveSession(session: UserSession) {
        preferences.edit()
            .putString(KEY_USER_ID, session.userId)
            .putString(KEY_EMAIL, session.email)
            .putString(KEY_DISPLAY_NAME, session.displayName)
            .putString(KEY_TOKEN, session.token)
            .apply()
    }

    fun getCurrentUser(): UserSession? {
        val userId = preferences.getString(KEY_USER_ID, null) ?: return null
        val email = preferences.getString(KEY_EMAIL, null) ?: return null
        return UserSession(
            userId = userId,
            email = email,
            displayName = preferences.getString(KEY_DISPLAY_NAME, null),
            token = preferences.getString(KEY_TOKEN, null)
        )
    }

    fun isLoggedIn(): Boolean = getCurrentUser() != null

    fun logout() {
        preferences.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_TOKEN)
            .apply()
    }

    fun saveLastSync(timestamp: Long) {
        preferences.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }

    fun getLastSync(): Long = preferences.getLong(KEY_LAST_SYNC, 0L)

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_EMAIL = "email"
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_TOKEN = "token"
        const val KEY_LAST_SYNC = "last_sync"
    }
}
