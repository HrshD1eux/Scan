package com.HrshD1eux.Scan.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val AUTO_FLASHLIGHT = booleanPreferencesKey("auto_flashlight")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SOUND_FEEDBACK = booleanPreferencesKey("sound_feedback")
        val SAVE_HISTORY = booleanPreferencesKey("save_history")
    }

    val autoFlashlightFlow: Flow<Boolean> = context.dataStore.data.map { it[AUTO_FLASHLIGHT] ?: true }
    val hapticFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { it[HAPTIC_FEEDBACK] ?: true }
    val soundFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { it[SOUND_FEEDBACK] ?: true }
    val saveHistoryFlow: Flow<Boolean> = context.dataStore.data.map { it[SAVE_HISTORY] ?: true }

    suspend fun setAutoFlashlight(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_FLASHLIGHT] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setSoundFeedback(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_FEEDBACK] = enabled }
    }

    suspend fun setSaveHistory(enabled: Boolean) {
        context.dataStore.edit { it[SAVE_HISTORY] = enabled }
    }
}
