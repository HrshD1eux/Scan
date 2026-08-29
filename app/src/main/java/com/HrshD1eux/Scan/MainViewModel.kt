package com.HrshD1eux.Scan

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.HrshD1eux.Scan.history.HistoryDatabase
import com.HrshD1eux.Scan.history.HistoryEntity
import com.HrshD1eux.Scan.parser.ContentClassifier
import com.HrshD1eux.Scan.parser.ParsedContent
import com.HrshD1eux.Scan.scanner.DuplicateDetector
import com.HrshD1eux.Scan.ui.settings.SettingsManager
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Scanner : Screen
    data object Settings : Screen
    data object History : Screen
    data object About : Screen
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context get() = getApplication<Application>().applicationContext
    val settingsManager = SettingsManager(context)
    private val historyDao = HistoryDatabase.getDatabase(context).historyDao()
    private val duplicateDetector = DuplicateDetector()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Scanner)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _scannedContent = MutableStateFlow<ParsedContent?>(null)
    val scannedContent: StateFlow<ParsedContent?> = _scannedContent.asStateFlow()

    private val _detectedBarcodes = MutableStateFlow<List<Barcode>>(emptyList())
    val detectedBarcodes: StateFlow<List<Barcode>> = _detectedBarcodes.asStateFlow()

    val dynamicColors = settingsManager.dynamicColorsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val autoFlashlight = settingsManager.autoFlashlightFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val torchSuggestion = settingsManager.torchSuggestionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun onScanResult(barcodes: List<Barcode>) {
        if (barcodes.isEmpty()) return
        if (barcodes.size == 1) {
            processBarcode(barcodes.first())
        } else {
            _detectedBarcodes.value = barcodes
        }
    }

    fun selectBarcodeFromMultiple(barcode: Barcode) {
        _detectedBarcodes.value = emptyList()
        processBarcode(barcode)
    }

    fun dismissMultiCodeSelection() {
        _detectedBarcodes.value = emptyList()
    }

    fun resetScanner() {
        _scannedContent.value = null
        _detectedBarcodes.value = emptyList()
        duplicateDetector.clear()
    }

    fun processBarcode(barcode: Barcode) {
        val rawValue = barcode.rawValue ?: return
        if (duplicateDetector.isDuplicate(rawValue)) return

        viewModelScope.launch {
            val settings = settingsManager.getSettingsSnapshot()
            triggerFeedback(settings)
            val content = ContentClassifier.classify(barcode)
            _detectedBarcodes.value = emptyList()

            if (settings.autoCopy) {
                copyToClipboard(rawValue)
            }

            if (settings.saveHistory) {
                val entity = HistoryEntity(
                    type = content::class.simpleName ?: "Unknown",
                    primaryValue = content.primaryValue,
                    rawValue = rawValue
                )
                historyDao.insert(entity)
            }

            if (settings.batchScanMode) {
                Toast.makeText(context, "Scanned: ${content.displaySummary}", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (settings.autoOpenUrls && content is ParsedContent.Url) {
                openUrl(content.url)
                return@launch
            }

            _scannedContent.value = content
        }
    }

    private var toneGenerator: ToneGenerator? = null

    private fun triggerFeedback(settings: com.HrshD1eux.Scan.ui.settings.UserSettings) {
        if (settings.hapticFeedback) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(50)
                }
            }
        }

        if (settings.soundFeedback) {
            try {
                if (toneGenerator == null) {
                    toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                }
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
            } catch (_: Exception) {
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {
        }
    }

    private fun copyToClipboard(text: String) {
        if (text.isBlank()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Scanned Code", text)
        clipboard?.setPrimaryClip(clip)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUrl(url: String) {
        val uri = Uri.parse(url)
        try {
            CustomTabsIntent.Builder().build().launchUrl(context, uri)
        } catch (_: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
