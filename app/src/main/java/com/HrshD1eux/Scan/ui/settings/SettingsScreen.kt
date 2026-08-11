package com.HrshD1eux.Scan.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(settingsManager: SettingsManager, onBack: () -> Unit, onAboutClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    
    val autoFlashlight by settingsManager.autoFlashlightFlow.collectAsState(initial = true)
    val haptic by settingsManager.hapticFeedbackFlow.collectAsState(initial = true)
    val sound by settingsManager.soundFeedbackFlow.collectAsState(initial = true)
    val history by settingsManager.saveHistoryFlow.collectAsState(initial = true)
    val autoOpenUrls by settingsManager.autoOpenUrlsFlow.collectAsState(initial = false)
    val batchScanMode by settingsManager.batchScanModeFlow.collectAsState(initial = false)
    val torchSuggestion by settingsManager.torchSuggestionFlow.collectAsState(initial = true)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        SettingSwitch("Automatic Flashlight (Low Light)", autoFlashlight) {
            scope.launch { settingsManager.setAutoFlashlight(it) }
        }
        SettingSwitch("Haptic Feedback on Scan", haptic) {
            scope.launch { settingsManager.setHapticFeedback(it) }
        }
        SettingSwitch("Sound on Scan", sound) {
            scope.launch { settingsManager.setSoundFeedback(it) }
        }
        SettingSwitch("Save Scan History", history) {
            scope.launch { settingsManager.setSaveHistory(it) }
        }
        SettingSwitch("Batch Scan Mode (Continuous)", batchScanMode) {
            scope.launch { settingsManager.setBatchScanMode(it) }
        }
        SettingSwitch("Auto-Open Scanned URLs", autoOpenUrls) {
            scope.launch { settingsManager.setAutoOpenUrls(it) }
        }
        SettingSwitch("Suggest Flashlight in Low Light", torchSuggestion) {
            scope.launch { settingsManager.setTorchSuggestion(it) }
        }
        
        
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onAboutClick, modifier = Modifier.fillMaxWidth()) {
            Text("About & Check for Updates")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Text("Back")
        }
    }
}

@Composable
fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
