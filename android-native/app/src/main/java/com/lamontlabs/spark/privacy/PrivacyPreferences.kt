package com.lamontlabs.spark.privacy

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sparkPreferences by preferencesDataStore(name = "spark-privacy")

/** Preferences remain in the app sandbox. No account, analytics or remote sync. */
class PrivacyPreferences(context: Context) {
    private val store = context.applicationContext.sparkPreferences
    private val resurfacingKey = booleanPreferencesKey("resurfacing_enabled")

    // Privacy-first default: resurfacing is off until explicitly enabled.
    val resurfacingEnabled: Flow<Boolean> =
        store.data.map { preferences -> preferences[resurfacingKey] ?: false }

    suspend fun setResurfacingEnabled(enabled: Boolean) {
        store.edit { it[resurfacingKey] = enabled }
    }
}
