package com.example.routinetrack.data.preferences

import android.content.Context
import android.net.Uri
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfilePreferenceRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _profilePhotoPath = MutableStateFlow(prefs.getString(KEY_PROFILE_PHOTO_PATH, null))
    private val _birthday = MutableStateFlow(prefs.getString(KEY_BIRTHDAY, null))

    val profilePhotoPath: StateFlow<String?> = _profilePhotoPath.asStateFlow()
    val birthday: StateFlow<String?> = _birthday.asStateFlow()

    fun saveProfilePhoto(sourceUri: Uri): Result<String> {
        return runCatching {
            val profileDir = File(appContext.filesDir, "profile").apply { mkdirs() }
            val targetFile = File(profileDir, "profile_photo.jpg")
            appContext.contentResolver.openInputStream(sourceUri).use { input ->
                requireNotNull(input) { "Immagine non leggibile" }
                targetFile.outputStream().use { output -> input.copyTo(output) }
            }
            val path = targetFile.absolutePath
            prefs.edit().putString(KEY_PROFILE_PHOTO_PATH, path).apply()
            _profilePhotoPath.value = path
            path
        }
    }

    fun saveBirthday(date: LocalDate) {
        val value = date.toString()
        prefs.edit().putString(KEY_BIRTHDAY, value).apply()
        _birthday.value = value
    }

    private companion object {
        const val PREFS_NAME = "routine_profile_preferences"
        const val KEY_PROFILE_PHOTO_PATH = "profile_photo_path"
        const val KEY_BIRTHDAY = "birthday"
    }
}
