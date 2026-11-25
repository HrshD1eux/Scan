package com.HrshD1eux.Scan.ui.settings

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.HrshD1eux.Scan.ui.components.SettingSwitch
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
    val autoCopy by settingsManager.autoCopyFlow.collectAsState(initial = false)
    val dynamicColors by settingsManager.dynamicColorsFlow.collectAsState(initial = true)
    val productSearchEngine by settingsManager.productSearchEngineFlow.collectAsState(initial = "DuckDuckGo")
    val customSearchUrl by settingsManager.customSearchUrlFlow.collectAsState(initial = "")
    val autoPurgeDays by settingsManager.autoPurgeDaysFlow.collectAsState(initial = 0)

    val engines = listOf("DuckDuckGo", "Open Food Facts", "Amazon", "Google", "Custom")
    var engineMenuExpanded by remember { mutableStateOf(false) }

    val purgeOptions = listOf(0 to "Never (Keep forever)", 7 to "After 7 days", 30 to "After 30 days", 90 to "After 90 days")
    var purgeMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        Text("Scanner & Hardware", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(8.dp))

        SettingSwitch("Automatic Flashlight (Low Light)", autoFlashlight) {
            scope.launch { settingsManager.setAutoFlashlight(it) }
        }
        SettingSwitch("Haptic Feedback on Scan", haptic) {
            scope.launch { settingsManager.setHapticFeedback(it) }
        }
        SettingSwitch("Sound on Scan", sound) {
            scope.launch { settingsManager.setSoundFeedback(it) }
        }
        SettingSwitch("Suggest Flashlight in Low Light", torchSuggestion) {
            scope.launch { settingsManager.setTorchSuggestion(it) }
        }
        SettingSwitch("Batch Scan Mode (Continuous HUD)", batchScanMode) {
            scope.launch { settingsManager.setBatchScanMode(it) }
        }
        SettingSwitch("Auto-Open Scanned URLs", autoOpenUrls) {
            scope.launch { settingsManager.setAutoOpenUrls(it) }
        }
        SettingSwitch("Auto-Copy Scanned Codes", autoCopy) {
            scope.launch { settingsManager.setAutoCopy(it) }
        }
        SettingSwitch(
            label = "Use Wallpaper Colors (Material You)",
            checked = dynamicColors,
            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            scope.launch { settingsManager.setDynamicColors(it) }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Search & Privacy", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(12.dp))

        SettingSwitch("Save Scan History", history) {
            scope.launch { settingsManager.setSaveHistory(it) }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Product search engine dropdown
        Text("Product Barcode Search Engine", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = engineMenuExpanded,
            onExpandedChange = { engineMenuExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = productSearchEngine,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = engineMenuExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = engineMenuExpanded,
                onDismissRequest = { engineMenuExpanded = false }
            ) {
                engines.forEach { engine ->
                    DropdownMenuItem(
                        text = { Text(engine) },
                        onClick = {
                            scope.launch { settingsManager.setProductSearchEngine(engine) }
                            engineMenuExpanded = false
                        }
                    )
                }
            }
        }

        if (productSearchEngine == "Custom") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customSearchUrl,
                onValueChange = { scope.launch { settingsManager.setCustomSearchUrl(it) } },
                label = { Text("Custom Search URL") },
                placeholder = { Text("https://example.com/search?q={barcode}") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Auto purge history dropdown
        Text("Auto-Purge Scan History", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = purgeMenuExpanded,
            onExpandedChange = { purgeMenuExpanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            val currentPurgeLabel = purgeOptions.firstOrNull { it.first == autoPurgeDays }?.second ?: "Never"
            OutlinedTextField(
                value = currentPurgeLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = purgeMenuExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = purgeMenuExpanded,
                onDismissRequest = { purgeMenuExpanded = false }
            ) {
                purgeOptions.forEach { (days, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            scope.launch { settingsManager.setAutoPurgeDays(days) }
                            purgeMenuExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onAboutClick, modifier = Modifier.fillMaxWidth()) {
            Text("About & Check for Updates")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Back")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
