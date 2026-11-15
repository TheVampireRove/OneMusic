package com.example.diagnostics

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

private val Context.crashPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "crash_diagnostics_prefs")

/**
 * DataStore repository managing user preferences for crash diagnostics & logging.
 */
class CrashDiagnosticsPreferences private constructor(private val context: Context) {

    companion object {
        val KEY_SAVE_CRASH_LOGS = booleanPreferencesKey("save_crash_logs_and_diagnostics")

        @Volatile
        private var instance: CrashDiagnosticsPreferences? = null

        fun getInstance(context: Context): CrashDiagnosticsPreferences {
            return instance ?: synchronized(this) {
                instance ?: CrashDiagnosticsPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    /**
     * Flow tracking whether automatic crash reporting is enabled (default true).
     */
    val isCrashLoggingEnabled: Flow<Boolean> = context.crashPreferencesDataStore.data.map { preferences ->
        preferences[KEY_SAVE_CRASH_LOGS] ?: true
    }

    /**
     * Synchronous check used during uncaught exception handler execution.
     */
    fun isCrashLoggingEnabledSync(): Boolean {
        return try {
            val file = File(context.filesDir, "datastore/crash_diagnostics_prefs.preferences_pb")
            // If file does not exist yet, default is enabled (true)
            if (!file.exists()) return true
            // Read from SharedPreferences fallback or cached value if any
            val sp = context.getSharedPreferences("crash_diagnostics_cache", Context.MODE_PRIVATE)
            sp.getBoolean("save_crash_logs", true)
        } catch (_: Exception) {
            true
        }
    }

    suspend fun setCrashLoggingEnabled(enabled: Boolean) {
        context.crashPreferencesDataStore.edit { preferences ->
            preferences[KEY_SAVE_CRASH_LOGS] = enabled
        }
        try {
            val sp = context.getSharedPreferences("crash_diagnostics_cache", Context.MODE_PRIVATE)
            sp.edit().putBoolean("save_crash_logs", enabled).apply()
        } catch (_: Exception) {}
    }
}
