package com.HrshD1eux.Scan.ui.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class UserSettings(
    val autoFlashlight: Boolean = true,
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = true,
    val saveHistory: Boolean = true,
    val autoOpenUrls: Boolean = false,
    val batchScanMode: Boolean = false,
    val torchSuggestion: Boolean = true,
    val autoCopy: Boolean = false,
    val dynamicColors: Boolean = true,
    val productSearchEngine: String = "DuckDuckGo",
    val customSearchUrl: String = "",
    val autoPurgeDays: Int = 0
)

val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val AUTO_FLASHLIGHT = booleanPreferencesKey("auto_flashlight")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val SOUND_FEEDBACK = booleanPreferencesKey("sound_feedback")
        val SAVE_HISTORY = booleanPreferencesKey("save_history")
        val AUTO_OPEN_URLS = booleanPreferencesKey("auto_open_urls")
        val BATCH_SCAN_MODE = booleanPreferencesKey("batch_scan_mode")
        val TORCH_SUGGESTION = booleanPreferencesKey("torch_suggestion")
        val AUTO_COPY = booleanPreferencesKey("auto_copy")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val PRODUCT_SEARCH_ENGINE = stringPreferencesKey("product_search_engine")
        val CUSTOM_SEARCH_URL = stringPreferencesKey("custom_search_url")
        val AUTO_PURGE_DAYS = intPreferencesKey("auto_purge_days")
    }

    val settingsSnapshotFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            autoFlashlight = prefs[AUTO_FLASHLIGHT] ?: true,
            hapticFeedback = prefs[HAPTIC_FEEDBACK] ?: true,
            soundFeedback = prefs[SOUND_FEEDBACK] ?: true,
            saveHistory = prefs[SAVE_HISTORY] ?: true,
            autoOpenUrls = prefs[AUTO_OPEN_URLS] ?: false,
            batchScanMode = prefs[BATCH_SCAN_MODE] ?: false,
            torchSuggestion = prefs[TORCH_SUGGESTION] ?: true,
            autoCopy = prefs[AUTO_COPY] ?: false,
            dynamicColors = prefs[DYNAMIC_COLORS] ?: true,
            productSearchEngine = prefs[PRODUCT_SEARCH_ENGINE] ?: "DuckDuckGo",
            customSearchUrl = prefs[CUSTOM_SEARCH_URL] ?: "",
            autoPurgeDays = prefs[AUTO_PURGE_DAYS] ?: 0
        )
    }

    val autoFlashlightFlow: Flow<Boolean> = context.dataStore.data.map { it[AUTO_FLASHLIGHT] ?: true }
    val hapticFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { it[HAPTIC_FEEDBACK] ?: true }
    val soundFeedbackFlow: Flow<Boolean> = context.dataStore.data.map { it[SOUND_FEEDBACK] ?: true }
    val saveHistoryFlow: Flow<Boolean> = context.dataStore.data.map { it[SAVE_HISTORY] ?: true }
    val autoOpenUrlsFlow: Flow<Boolean> = context.dataStore.data.map { it[AUTO_OPEN_URLS] ?: false }
    val batchScanModeFlow: Flow<Boolean> = context.dataStore.data.map { it[BATCH_SCAN_MODE] ?: false }
    val torchSuggestionFlow: Flow<Boolean> = context.dataStore.data.map { it[TORCH_SUGGESTION] ?: true }
    val autoCopyFlow: Flow<Boolean> = context.dataStore.data.map { it[AUTO_COPY] ?: false }
    val dynamicColorsFlow: Flow<Boolean> = context.dataStore.data.map { it[DYNAMIC_COLORS] ?: true }
    val productSearchEngineFlow: Flow<String> = context.dataStore.data.map { it[PRODUCT_SEARCH_ENGINE] ?: "DuckDuckGo" }
    val customSearchUrlFlow: Flow<String> = context.dataStore.data.map { it[CUSTOM_SEARCH_URL] ?: "" }
    val autoPurgeDaysFlow: Flow<Int> = context.dataStore.data.map { it[AUTO_PURGE_DAYS] ?: 0 }

    suspend fun getSettingsSnapshot(): UserSettings = settingsSnapshotFlow.first()

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

    suspend fun setAutoOpenUrls(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_OPEN_URLS] = enabled }
    }

    suspend fun setBatchScanMode(enabled: Boolean) {
        context.dataStore.edit { it[BATCH_SCAN_MODE] = enabled }
    }

    suspend fun setTorchSuggestion(enabled: Boolean) {
        context.dataStore.edit { it[TORCH_SUGGESTION] = enabled }
    }

    suspend fun setAutoCopy(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_COPY] = enabled }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[DYNAMIC_COLORS] = enabled }
    }

    suspend fun setProductSearchEngine(engine: String) {
        context.dataStore.edit { it[PRODUCT_SEARCH_ENGINE] = engine }
    }

    suspend fun setCustomSearchUrl(url: String) {
        context.dataStore.edit { it[CUSTOM_SEARCH_URL] = url }
    }

    suspend fun setAutoPurgeDays(days: Int) {
        context.dataStore.edit { it[AUTO_PURGE_DAYS] = days }
    }
}
