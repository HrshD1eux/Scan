package com.HrshD1eux.Scan

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.HrshD1eux.Scan.ui.theme.ScanTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    private val activityIntent = mutableStateOf<Intent?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        activityIntent.value = intent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityIntent.value = intent
        setContent {
            val context = LocalContext.current
            val settingsManager = remember { SettingsManager(context) }
            val dynamicColorsEnabled by settingsManager.dynamicColorsFlow.collectAsState(initial = true)

            ScanTheme(dynamicColor = dynamicColorsEnabled) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentIntent by activityIntent
                    var scannedContent by remember { mutableStateOf<ParsedContent?>(null) }
                    var detectedBarcodes by remember { mutableStateOf<List<Barcode>>(emptyList()) }
                    var currentScreen by remember { mutableStateOf("scanner") } // "scanner", "settings", "about", "history"
                    
                    val actionResolver = remember { ActionResolver(context) }
                    val historyDao = remember { HistoryDatabase.getDatabase(context).historyDao() }
                    val duplicateDetector = remember { DuplicateDetector() }
                    
                    val hapticEnabled by settingsManager.hapticFeedbackFlow.collectAsState(initial = true)
                    val soundEnabled by settingsManager.soundFeedbackFlow.collectAsState(initial = true)
                    val autoOpenUrls by settingsManager.autoOpenUrlsFlow.collectAsState(initial = false)
                    val batchScanMode by settingsManager.batchScanModeFlow.collectAsState(initial = false)
                    val autoCopyEnabled by settingsManager.autoCopyFlow.collectAsState(initial = false)

                    // Extracted processing function
                    fun processSingleBarcode(barcode: Barcode) {
                        val rawValue = barcode.rawValue ?: return
                        if (duplicateDetector.isDuplicate(rawValue)) return

                        // Haptic / Sound feedback
                        if (hapticEnabled) {
                            val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(50)
                            }
                        }
                        if (soundEnabled) {
                            try {
                                val tg = android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100)
                                tg.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 100)
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }

                        val content = ContentClassifier.classify(barcode)
                        detectedBarcodes = emptyList() // clear list once selected
                        
                        // Auto-copy if enabled
                        if (autoCopyEnabled) {
                            val textToCopy = barcode.rawValue ?: ""
                            if (textToCopy.isNotEmpty()) {
                                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Scanned Code", textToCopy)
                                clipboardManager.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Auto-copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }

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
                                    is ParsedContent.Otp -> content.label
                                    is ParsedContent.Contact -> content.name
                                    is ParsedContent.Geo -> "${content.latitude}, ${content.longitude}"
                                    is ParsedContent.Sms -> content.phoneNumber
                                }
                                val typeName = content::class.simpleName ?: "Unknown"
                                historyDao.insert(HistoryEntity(type = typeName, primaryValue = primaryValue))
                            }
                        }

                        if (batchScanMode) {
                            val displayValue = when(content) {
                                is ParsedContent.Url -> content.url
                                is ParsedContent.Upi -> content.upiId
                                is ParsedContent.Wifi -> content.ssid
                                is ParsedContent.Phone -> content.number
                                is ParsedContent.Email -> content.address
                                is ParsedContent.Product -> content.barcode
                                is ParsedContent.UnknownBarcode -> content.rawValue
                                is ParsedContent.Text -> content.text.take(20) + if(content.text.length > 20) "..." else ""
                                is ParsedContent.Otp -> content.label
                                is ParsedContent.Contact -> content.name
                                is ParsedContent.Geo -> "${content.latitude}, ${content.longitude}"
                                is ParsedContent.Sms -> content.phoneNumber
                            }
                            android.widget.Toast.makeText(context, "Scanned: $displayValue", android.widget.Toast.LENGTH_SHORT).show()
                            return
                        }

                        if (autoOpenUrls && content is ParsedContent.Url) {
                            val uri = android.net.Uri.parse(content.url)
                            try {
                                androidx.browser.customtabs.CustomTabsIntent.Builder().build().launchUrl(context, uri)
                            } catch (e: Exception) {
                                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                            return
                        }

                        scannedContent = content
                    }

                    val galleryLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        if (uri != null) {
                            com.HrshD1eux.Scan.camera.GalleryScanner.scanImage(context, uri) { barcodes ->
                                if (barcodes.isNotEmpty()) {
                                    if (barcodes.size == 1) {
                                        processSingleBarcode(barcodes.first())
                                    } else {
                                        detectedBarcodes = barcodes
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(currentIntent) {
                        val activeIntent = currentIntent
                        if (activeIntent?.action == android.content.Intent.ACTION_SEND && activeIntent.type?.startsWith("image/") == true) {
                            @Suppress("DEPRECATION")
                            val uri = activeIntent.getParcelableExtra<android.net.Uri>(android.content.Intent.EXTRA_STREAM)
                            if (uri != null) {
                                com.HrshD1eux.Scan.camera.GalleryScanner.scanImage(context, uri) { barcodes ->
                                    if (barcodes.isNotEmpty()) {
                                        if (barcodes.size == 1) {
                                            processSingleBarcode(barcodes.first())
                                        } else {
                                            detectedBarcodes = barcodes
                                        }
                                    }
                                }
                            }
                        } else if (activeIntent?.action == "com.HrshD1eux.Scan.ACTION_SCAN_GALLERY") {
                            galleryLauncher.launch("image/*")
                        } else if (activeIntent?.action == "com.HrshD1eux.Scan.ACTION_VIEW_HISTORY") {
                            currentScreen = "history"
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
