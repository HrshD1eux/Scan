package com.HrshD1eux.Scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.lifecycle.lifecycleScope
import com.HrshD1eux.Scan.actions.ActionResolver
import com.HrshD1eux.Scan.history.HistoryDatabase
import com.HrshD1eux.Scan.history.HistoryEntity
import com.HrshD1eux.Scan.parser.ContentClassifier
import com.HrshD1eux.Scan.parser.ParsedContent
import com.HrshD1eux.Scan.ui.history.HistoryScreen
import com.HrshD1eux.Scan.ui.result.ResultScreen
import com.HrshD1eux.Scan.ui.scanner.ScannerScreen
import com.HrshD1eux.Scan.scanner.DuplicateDetector
import com.HrshD1eux.Scan.ui.settings.AboutScreen
import com.HrshD1eux.Scan.ui.settings.SettingsManager
import com.HrshD1eux.Scan.ui.settings.SettingsScreen
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    var scannedContent by remember { mutableStateOf<ParsedContent?>(null) }
                    var detectedBarcodes by remember { mutableStateOf<List<Barcode>>(emptyList()) }
                    var currentScreen by remember { mutableStateOf("scanner") } // "scanner", "settings", "about", "history"
                    
                    val actionResolver = remember { ActionResolver(context) }
                    val historyDao = remember { HistoryDatabase.getDatabase(context).historyDao() }
                    val settingsManager = remember { SettingsManager(context) }
                    val duplicateDetector = remember { DuplicateDetector() }

                    // Extracted processing function
                    fun processSingleBarcode(barcode: Barcode) {
                        val rawValue = barcode.rawValue ?: return
                        if (duplicateDetector.isDuplicate(rawValue)) return

                        val content = ContentClassifier.classify(barcode)
                        scannedContent = content
                        detectedBarcodes = emptyList() // clear list once selected
                        
                        // Save history if enabled
                        lifecycleScope.launch {
                            val saveHistory = settingsManager.saveHistoryFlow.first()
                            if (saveHistory) {
                                val primaryValue = when(content) {
                                    is ParsedContent.Url -> content.url
                                    is ParsedContent.Upi -> content.upiId
                                    is ParsedContent.Wifi -> content.ssid
                                    is ParsedContent.Phone -> content.number
                                    is ParsedContent.Email -> content.address
                                    is ParsedContent.Product -> content.barcode
                                    is ParsedContent.UnknownBarcode -> content.rawValue
                                    is ParsedContent.Text -> content.text
                                }
                                val typeName = content::class.simpleName ?: "Unknown"
                                historyDao.insert(HistoryEntity(type = typeName, primaryValue = primaryValue))
                            }
                        }
                    }

                    when (currentScreen) {
                        "scanner" -> {
                            if (scannedContent == null && detectedBarcodes.isEmpty()) {
                                ScannerScreen(
                                    onScanSuccess = { barcodes ->
                                        if (barcodes.size == 1) {
                                            processSingleBarcode(barcodes.first())
                                        } else if (barcodes.size > 1) {
                                            detectedBarcodes = barcodes
                                        }
                                    },
                                    onSettingsClick = { currentScreen = "settings" },
                                    onHistoryClick = { currentScreen = "history" }
                                )
                            } else if (detectedBarcodes.isNotEmpty()) {
                                // Multi-code selection UI
                                androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                    Text("Multiple codes detected. Please select one:", style = MaterialTheme.typography.headlineSmall)
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                                    detectedBarcodes.forEach { barcode ->
                                        androidx.compose.material3.Card(
                                            onClick = { processSingleBarcode(barcode) },
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = barcode.rawValue ?: "Unknown",
                                                modifier = Modifier.padding(16.dp),
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))
                                    androidx.compose.material3.Button(onClick = { detectedBarcodes = emptyList() }) {
                                        Text("Cancel")
                                    }
                                }
                            } else {
                                ResultScreen(
                                    content = scannedContent!!,
                                    actions = actionResolver.resolve(scannedContent!!),
                                    onScanAgain = { 
                                        scannedContent = null 
                                        duplicateDetector.clear()
                                    }
                                )
                            }
                        }
                        "settings" -> {
                            SettingsScreen(
                                settingsManager = settingsManager,
                                onBack = { currentScreen = "scanner" },
                                onAboutClick = { currentScreen = "about" }
                            )
                        }
                        "history" -> {
                            HistoryScreen(
                                onBack = { currentScreen = "scanner" }
                            )
                        }
                        "about" -> {
                            AboutScreen(
                                onBack = { currentScreen = "settings" }
                            )
                        }
                    }
                }
            }
        }
    }
}
