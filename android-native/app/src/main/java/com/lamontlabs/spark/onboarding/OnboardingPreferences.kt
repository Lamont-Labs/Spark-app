package com.lamontlabs.spark.onboarding

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingStore by preferencesDataStore(name = "spark-onboarding")

class OnboardingPreferences(private val context: Context) {
    private val completedKey = booleanPreferencesKey("completed")
    private val stepKey = intPreferencesKey("step")
    val completed: Flow<Boolean> = context.onboardingStore.data.map { it[completedKey] ?: false }
    val step: Flow<Int> = context.onboardingStore.data.map { (it[stepKey] ?: 0).coerceIn(0, 4) }

    suspend fun setStep(value: Int) {
        context.onboardingStore.edit { it[stepKey] = value.coerceIn(0, 4) }
    }

    suspend fun finish() {
        context.onboardingStore.edit {
            it[completedKey] = true
            it[stepKey] = 4
        }
    }
}
